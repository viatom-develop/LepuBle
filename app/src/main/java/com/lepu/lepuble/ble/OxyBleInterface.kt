package com.lepu.lepuble.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import android.util.Log
import androidx.annotation.NonNull
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.lepuble.ble.utils.BleCRC
import com.lepu.lepuble.ble.cmd.OxyBleCmd
import com.lepu.lepuble.ble.cmd.OxyBleResponse
import com.lepu.lepuble.ble.obj.OxyDataController
import com.lepu.lepuble.file.OxyDataFile
import com.lepu.lepuble.objs.Bluetooth
import com.lepu.lepuble.utils.add
import com.lepu.lepuble.utils.toHex
import com.lepu.lepuble.utils.toUInt
import com.lepu.lepuble.vals.EventMsgConst
import com.lepu.lepuble.viewmodel.MainViewModel
import com.lepu.lepuble.viewmodel.OxyViewModel
import kotlinx.coroutines.*
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import java.lang.Runnable
import java.util.*
import kotlin.concurrent.schedule
import kotlin.experimental.inv

class OxyBleInterface : ConnectionObserver, OxyBleManager.onNotifyListener {

    private var mainVM: MainViewModel? = null
    fun setMainVM(model: MainViewModel) {
        mainVM = model
    }

    private lateinit var model: OxyViewModel
    fun setViewModel(viewModel: OxyViewModel) {
        this.model = viewModel
    }

    private var curCmd: Int = 0
    private var timeout: Job? = null

    lateinit var manager: OxyBleManager
    lateinit var mydevice: BluetoothDevice

    private var oxyInfo: OxyBleResponse.OxyInfo? = null

    private var pool: ByteArray? = null
    private var count: Int = 0

    private val rtHandler = Handler()
    inner class RtTask: Runnable {
        override fun run() {

            count++
//            LogUtils.d("RtTask: $count")

            if (state) {
                rtHandler.postDelayed(this, 1000)
                getRtData()
            }
        }
    }

    public var state = false
    private var connecting = false

    public fun connect(context: Context, @NonNull device: BluetoothDevice) {
        if (connecting || state) {
            return
        }
        LogUtils.d("try connect: ${device.name}")
        manager = OxyBleManager(context)
        mydevice = device
        manager.connectionObserver = this
        manager.setNotifyListener(this)
        manager.connect(device)
                .useAutoConnect(true)
                .timeout(10000)
                .retry(3, 100)
                .done {
                    LogUtils.d("Device Init")
                }
                .enqueue()

    }

    /**
     * 用于测试
     */
    public fun sendCmd(bs: ByteArray) {
        manager.sendCmd(bs)
    }

    private fun sendCmd(cmd: Int, bs: ByteArray) {
        if (!state) {
            return
        }
//        LogUtils.d("try send cmd: $cmd, ${bs.toHex()}")
        if (curCmd != 0) {
            // busy
            LogUtils.d("busy: $cmd =>$curCmd")
            return
        }

        curCmd = cmd
        pool = null
        manager.sendCmd(bs)
        timeout = GlobalScope.launch {
            delay(3000)
            // timeout
            LogUtils.d("timeout: $curCmd")
            when(curCmd) {

                OxyBleCmd.OXY_CMD_PARA_SYNC -> {
                    curCmd = 0
                    getInfo()
                }

                OxyBleCmd.OXY_CMD_INFO -> {
                    curCmd = 0
                    getInfo()
                }
//                OxyBleCmd.OXY_CMD_RT_DATA -> {
//                    curCmd = 0
//                }
                else -> {
                    curCmd = 0
                }
            }
        }
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: OxyBleResponse.OxyResponse) {
//        LogUtils.d("Response: $curCmd, ${response.content.toHex()}")
        if (curCmd == 0) {
            return
        }


        when(curCmd) {
            OxyBleCmd.OXY_CMD_PARA_SYNC -> {
                clearTimeout()

                getInfo()
            }

            OxyBleCmd.OXY_CMD_INFO -> {
                clearTimeout()

                val info = OxyBleResponse.OxyInfo(response.content)
                model.info.value = info

                LiveEventBus.get(EventMsgConst.EventOxyInfo)
                    .post(info)
//                model.battery.value = info.battery
//                downloadFiles(oxyInfo = info)
            }

            OxyBleCmd.OXY_CMD_RT_DATA -> {
                clearTimeout()

                val rtWave = OxyBleResponse.RtWave(response.content)
                model.battery.value = rtWave.battery
                model.pr.value = rtWave.pr
                model.spo2.value = rtWave.spo2
//                model.pi.value = rtWave.pi / 10.0f

//                LogUtils.d(response.content.toHex(), "battery: ${rtWave.battery}")
                OxyDataController.receive(rtWave.wFs)
//                LogUtils.d("Oxy Controller: ${OxyDataController.dataRec.size}")

                LiveEventBus.get(EventMsgConst.EventOxyRtData)
                    .post(rtWave)
            }
            OxyBleCmd.OXY_CMD_READ_START -> {
                clearTimeout()
                if (response.state) {
                    val fileSize = toUInt(response.content)
                    curFile = OxyBleResponse.OxyFile(curFileName!!, fileSize)
                    sendCmd(OxyBleCmd.OXY_CMD_READ_CONTENT, OxyBleCmd.readFileContent())
                    LogUtils.d("read file start：$curFileName")
                } else {
                    LogUtils.d("read file failed：${response.content.toHex()}")
                }
            }

            OxyBleCmd.OXY_CMD_READ_CONTENT -> {
                clearTimeout()
                curFile?.apply {
                    this.addContent(response.content)
                    LogUtils.d("read file：${curFile?.fileName}   => ${curFile?.index} / ${curFile?.fileSize}")
                    if (this.index < this.fileSize) {
                        sendCmd(OxyBleCmd.OXY_CMD_READ_CONTENT, OxyBleCmd.readFileContent())
                    } else {
                        sendCmd(OxyBleCmd.OXY_CMD_READ_END, OxyBleCmd.readFileEnd())
                    }
                }
            }
            OxyBleCmd.OXY_CMD_READ_END -> {
                clearTimeout()
                LogUtils.d("read file finished: ${curFile?.fileName} ==> ${curFile?.fileSize}")
                val oxyFile = OxyDataFile(curFile!!.fileContent)
                LogUtils.d(oxyFile)
                curFileName = null
                curFile = null
            }

            else -> {
                clearTimeout()
            }
        }
    }

    private fun clearVar() {
//        model.battery.value = 0
        model.pr.value = 0
        model.spo2.value = 0
//        model.pi.value = 0.0f
    }

    private fun clearTimeout() {
        curCmd = 0
        timeout?.cancel()
        timeout = null
    }

    @ExperimentalUnsignedTypes
    fun hasResponse(bytes: ByteArray?) : ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-7) {
            if (bytes[i] != 0x55.toByte() || bytes[i+1] != bytes[i+2].inv()) {
                continue@loop
            }

            // need content length
            val len = toUInt(bytes.copyOfRange(i+5, i+7))
//            Log.d(TAG, "want bytes length: $len")
            if (i+8+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+8+len)
            if (temp.last() == BleCRC.calCRC8(temp)) {
                val bleResponse = OxyBleResponse.OxyResponse(temp)
                LogUtils.d("get response: " + temp.toHex())
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i+8+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

    fun disconnect() {
        manager.disconnect()
        manager.close()

        this.onDeviceDisconnected(mydevice, ConnectionObserver.REASON_SUCCESS)
    }

    fun syncTime() {
        sendCmd(OxyBleCmd.OXY_CMD_PARA_SYNC, OxyBleCmd.syncTime())
    }

    fun getInfo() {
        sendCmd(OxyBleCmd.OXY_CMD_INFO, OxyBleCmd.getInfo())
    }

    fun getRtData() {
        sendCmd(OxyBleCmd.OXY_CMD_RT_DATA, OxyBleCmd.getRtWave())
    }

    fun runRtTask() {
        rtHandler.postDelayed(RtTask(), 200)
    }

    var curFileName: String? = null
    var curFile: OxyBleResponse.OxyFile? = null
    fun readFile(fileName: String) {
        curFileName = fileName
        sendCmd(OxyBleCmd.OXY_CMD_READ_START, OxyBleCmd.readFileStart(fileName))
    }

    fun downloadFiles(oxyInfo: OxyBleResponse.OxyInfo) {
        val files = oxyInfo.fileList.split(",")
        if (files.isNotEmpty()) {
            readFile(files[0])
        }
    }

    override fun onNotify(device: BluetoothDevice?, data: Data?) {
        data?.value?.apply {
            pool = add(pool, this)
        }
        pool?.apply {
            pool = hasResponse(pool)
        }
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        LogUtils.d("${device.name} connected")
        state = true
        model.connect.value = state

        connecting = false
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        LogUtils.d("${device.name} Connecting")
        state = false
        model.connect.value = state

        connecting = true
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        LogUtils.d("${device.name} Disconnected")
        state = false
        model.connect.value = state
        curCmd = 0
        rtHandler.removeCallbacks(RtTask())

        clearVar()

        connecting = false

        LiveEventBus.get(EventMsgConst.EventDeviceDisconnect).post(Bluetooth.MODEL_CHECKO2)
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        LogUtils.d("${device.name} Disconnecting")
        state = false
        model.connect.value = state
//        LogUtils.d(mydevice.name)

        connecting = false
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        LogUtils.d("${device.name} FailedToConnect")
        state = false
        model.connect.value = state

        connecting = false
    }

    override fun onDeviceReady(device: BluetoothDevice) {
//        runRtTask()
        LogUtils.d("${device.name} isReady")
        curCmd = 0

        connecting = false
        Timer().schedule(500) {
            syncTime()
        }
    }
}