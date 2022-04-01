package com.lepu.lepuble.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import androidx.annotation.NonNull
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.lepuble.ble.utils.BleCRC
import com.lepu.lepuble.ble.cmd.UniversalBleCmd
import com.lepu.lepuble.ble.cmd.Er1BleResponse
import com.lepu.lepuble.ble.cmd.Er3BleCmd
import com.lepu.lepuble.ble.cmd.Er3BleResponse
import com.lepu.lepuble.ble.obj.EcgDataController
import com.lepu.lepuble.ble.obj.LepuDevice
import com.lepu.lepuble.objs.Bluetooth
import com.lepu.lepuble.utils.add
import com.lepu.lepuble.utils.toInt
import com.lepu.lepuble.utils.toUInt
import com.lepu.lepuble.vals.EventMsgConst
import com.lepu.lepuble.viewmodel.Er3ViewModel
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import java.util.*
import kotlin.experimental.inv

class Er3BleInterface : ConnectionObserver, Er3BleManager.onNotifyListener {
    private lateinit var model: Er3ViewModel
    fun setViewModel(viewModel: Er3ViewModel) {
        this.model = viewModel
    }

    lateinit var manager: Er3BleManager

    lateinit var mydevice: BluetoothDevice

    private var pool: ByteArray? = null

    private val rtHandler = Handler()
    private var count: Int = 0
    inner class RtTask: Runnable {
        override fun run() {
            rtHandler.postDelayed(this, 1000)
            if (state) {
                count++
                getRtData()
//                LogUtils.d("RtTask: $count")
            }
        }
    }

    /**
     * interface
     * state
     * connect
     * disconnect
     * getInfo
     * getRtData
     */
    public var state = false
    private var connecting = false

    public fun connect(context: Context, @NonNull device: BluetoothDevice) {
        if (connecting || state) {
            return
        }
        LogUtils.d("try connect: ${device.name}")
        manager = Er3BleManager(context)
        mydevice = device
        manager.setConnectionObserver(this)
        manager.setNotifyListener(this)
        manager.connect(device)
                .useAutoConnect(true)
                .timeout(10000)
                .retry(3, 100)
                .done {
                    LogUtils.d("Device Init")
                    getConfig()
                }
                .enqueue()

    }

    public fun disconnect() {
        manager.disconnect().enqueue()
//        manager.close()

        this.onDeviceDisconnected(mydevice, ConnectionObserver.REASON_SUCCESS)
    }

    /**
     * get device info
     */
    public fun getInfo() {
        sendCmd(UniversalBleCmd.getInfo())
    }

    /**
     * get er3 real-time data
     * 旧协议
     */
    public fun getEr3RtData() {
        sendCmd(Er3BleCmd.getEr3RtData())
    }

    /**
     * get rt data
     */
    public fun getRtData() {
        sendCmd(Er3BleCmd.getRtData())
    }

    /**
     * run real-time task
     */
    public fun runRtTask() {
        rtHandler.postDelayed(RtTask(), 200)
    }

    /**
     * get file list
     */
    public fun getFileList() {
        sendCmd(UniversalBleCmd.getFileList())
    }

    /**
     * get config
     * 心电测量模式：
    0   监护模式
    1   手术模式
    2   ST模式
    */
    public fun getConfig() {
        sendCmd(Er3BleCmd.getConfig())
    }

    public fun setConfig(mode: Int) {
        sendCmd(Er3BleCmd.setConfig(mode))
    }

    /**
     * factory reset
     */
    public fun factoryReset() {
        sendCmd(UniversalBleCmd.factoryReset())
    }

    /**
     * download a file, name come from filelist
     */
    var curFileName: String? = null
    var curFile: Er1BleResponse.Er1File? = null
    var fileList: Er1BleResponse.Er1FileList? = null
    public fun downloadFile(name : ByteArray) {
        curFileName = String(name)
        sendCmd(UniversalBleCmd.readFileStart(name, 0))
    }

    private fun sendCmd(bs: ByteArray) {
        if (!state) {
            return
        }
        manager.sendCmd(bs)
//        LiveEventBus.get(EventMsgConst.EventBlePkg).post(1)
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: Er1BleResponse.Er1Response) {
//        LogUtils.d("received: ${response.cmd}")
        LiveEventBus.get(EventMsgConst.EventBlePkg).post(2)
        when(response.cmd) {
            UniversalBleCmd.GET_INFO -> {
                val erInfo = LepuDevice(response.content)
                model.er3.value = erInfo
                LogUtils.d(erInfo.toString())
                LiveEventBus.get(EventMsgConst.EventEr1Info)
                        .post(erInfo)

            }

            Er3BleCmd.ER3_RT_DATA -> {
                val rtData = Er3BleResponse.Er3RtData(response.content)
                model.hr.value = rtData.param.hr
                model.duration.value = rtData.param.recordTime
                model.lead.value = rtData.param.leadOn
                model.battery.value = rtData.param.battery

//                LogUtils.d("${rtData.param.hr} => ${rtData.wave.len}")

                rtData.wave.waveFs?.let { EcgDataController.receive(it) }
                LiveEventBus.get(EventMsgConst.EventEr1RtData)
                        .post(rtData)
            }

            Er3BleCmd.RT_DATA -> {
                val rtData = Er3BleResponse.RtData(response.content)
                LogUtils.d("RT Data received")
                model.hr.value = rtData.runStatus.hr
                model.spo2.value = rtData.runStatus.spo2
                model.temp.value = rtData.runStatus.temp
                model.battery.value = rtData.runStatus.battery

                rtData.rtWave.waveMvs?.let { EcgDataController.receive(it) }
            }

            Er3BleCmd.GET_CONFIG -> {
                response.content.apply {
                    if (this.isNotEmpty()) {
                        LiveEventBus.get(EventMsgConst.EventEr3GetConfig)
                            .post(toInt(this))
                    }
                }

            }

            Er3BleCmd.SET_CONFIG -> {
                getConfig()
            }

            UniversalBleCmd.READ_FILE_LIST -> {
                fileList = Er1BleResponse.Er1FileList(response.content)
                LogUtils.d(fileList.toString())
            }

            UniversalBleCmd.READ_FILE_START -> {
                if (response.pkgType == 0x01.toByte()) {
                    curFile = Er1BleResponse.Er1File(curFileName!!, toUInt(response.content))
                    sendCmd(UniversalBleCmd.readFileData(0))
                } else {
                    LogUtils.d("read file failed：${response.pkgType}")
                }
            }

            UniversalBleCmd.READ_FILE_DATA -> {
                curFile?.apply {
                    this.addContent(response.content)
                    LogUtils.d("read file：${curFile?.fileName}   => ${curFile?.index} / ${curFile?.fileSize}")

                    if (this.index < this.fileSize) {
                        sendCmd(UniversalBleCmd.readFileData(this.index))
                    } else {
                        sendCmd(UniversalBleCmd.readFileEnd())
                    }
                }
            }

            UniversalBleCmd.READ_FILE_END -> {
                LogUtils.d("read file finished: ${curFile?.fileName} ==> ${curFile?.fileSize}")
                curFileName = null
                curFile = null
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-7) {
            if (bytes[i] != 0xA5.toByte() || bytes[i+1] != bytes[i+2].inv()) {
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
                val bleResponse = Er1BleResponse.Er1Response(temp)
//                LogUtils.d("get response: ${temp.toHex()}" )
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i+8+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

    private fun clearVar() {
        model.battery.value = 0
        model.duration.value = 0
        model.hr.value = 0
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
        state = true
        model.connect.value = state
        LogUtils.d(mydevice.name)

        connecting = false
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        state = false
        model.connect.value = state
//        LogUtils.d(mydevice.name)

        connecting = true
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        state = false
        model.connect.value = state
        LogUtils.d(mydevice.name)
        rtHandler.removeCallbacks(RtTask())

        clearVar()

        connecting = false

        LiveEventBus.get(EventMsgConst.EventDeviceDisconnect).post(Bluetooth.MODEL_ER1)
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        state = false
        model.connect.value = state
//        LogUtils.d(mydevice.name)

        connecting = false
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        state = false
        LogUtils.d(mydevice.name)
        model.connect.value = state

        connecting = false
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        connecting = false
//        LogUtils.d(mydevice.name)
    }
}