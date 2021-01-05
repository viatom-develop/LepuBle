package com.lepu.lepuble.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import androidx.annotation.NonNull
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.lepuble.ble.cmd.Er1BleCRC
import com.lepu.lepuble.ble.cmd.Er1BleCmd
import com.lepu.lepuble.ble.cmd.Er1BleResponse
import com.lepu.lepuble.ble.cmd.S1BleResponse
import com.lepu.lepuble.ble.obj.Er1DataController
import com.lepu.lepuble.ble.obj.Er1Device
import com.lepu.lepuble.objs.Bluetooth
import com.lepu.lepuble.utils.add
import com.lepu.lepuble.utils.toUInt
import com.lepu.lepuble.vals.EventMsgConst
import com.lepu.lepuble.viewmodel.S1ViewModel
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import kotlin.experimental.inv

class S1BleInterface : ConnectionObserver, Er1BleManager.onNotifyListener {

    private lateinit var model: S1ViewModel
    fun setViewModel(viewModel: S1ViewModel) {
        this.model = viewModel
    }

    lateinit var manager: Er1BleManager

    lateinit var mydevice: BluetoothDevice

    private var pool: ByteArray? = null

    private val rtHandler = Handler()
    private var count: Int = 0
    inner class RtTask: Runnable {
        override fun run() {
            rtHandler.postDelayed(this, 500)
            if (state) {
                count++
                getRtData()
//                LogUtils.d("RtTask: $count")
            } else {
                LiveEventBus.get(EventMsgConst.EventEr1InvalidRtData)
                    .post(true)
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
    private var linkLost = true

    public fun connect(context: Context, @NonNull device: BluetoothDevice) {
        if (connecting || state || !linkLost) {
            return
        }
        LogUtils.d("try connect: ${device.name}")
        manager = Er1BleManager(context)
        mydevice = device
        manager.setConnectionObserver(this)
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

    public fun disconnect() {
        manager.disconnect()
        manager.close()
        linkLost = true

        this.onDeviceDisconnected(mydevice, ConnectionObserver.REASON_SUCCESS)
    }

    /**
     * get device info
     */
    public fun getInfo() {
        sendCmd(Er1BleCmd.getInfo())
    }

    /**
     * get real-time data
     */
    public fun getRtData() {
        sendCmd(Er1BleCmd.getRtData())
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
        sendCmd(Er1BleCmd.getFileList())
    }

    /**
     * download a file, name come from filelist
     */
    var curFileName: String? = null
    var curFile: Er1BleResponse.Er1File? = null
    var fileList: Er1BleResponse.Er1FileList? = null
    public fun downloadFile(name : ByteArray) {
        curFileName = String(name)
        sendCmd(Er1BleCmd.readFileStart(name, 0))
    }

    private fun sendCmd(bs: ByteArray) {
        if (!state) {
            return
        }
        manager.sendCmd(bs)
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: Er1BleResponse.Er1Response) {
//        LogUtils.d("received: ${response.cmd}")
        when(response.cmd) {
            Er1BleCmd.ER1_CMD_GET_INFO -> {
                val erInfo = Er1Device(response.content)
                model.er1.value = erInfo
                LiveEventBus.get(EventMsgConst.EventEr1Info)
                    .post(erInfo)
                runRtTask()
            }

            Er1BleCmd.ER1_CMD_RT_DATA -> {
                val rtData = S1BleResponse.S1RtData(response.content)
                model.hr.value = rtData.param.hr
                model.duration.value = rtData.param.recordTime
                model.lead.value = rtData.param.leadOn

                model.weight.value = rtData.scaleData.weight
                model.unit.value = rtData.scaleData.unit
                model.resistance.value = rtData.scaleData.resistance

                Er1DataController.receive(rtData.wave.wFs)
//                LogUtils.d("ER1 Controller: ${Er1DataController.dataRec.size}")
                LiveEventBus.get(EventMsgConst.EventEr1RtData)
                    .post(rtData)
            }

            Er1BleCmd.ER1_CMD_READ_FILE_LIST -> {
                fileList = Er1BleResponse.Er1FileList(response.content)
                LogUtils.d(fileList.toString())
            }

            Er1BleCmd.ER1_CMD_READ_FILE_START -> {
                if (response.pkgType == 0x01.toByte()) {
                    curFile = Er1BleResponse.Er1File(curFileName!!, toUInt(response.content))
                    sendCmd(Er1BleCmd.readFileData(0))
                } else {
                    LogUtils.d("读文件失败：${response.pkgType}")
                }
            }

            Er1BleCmd.ER1_CMD_READ_FILE_DATA -> {
                curFile?.apply {
                    this.addContent(response.content)
                    LogUtils.d("读文件：${curFile?.fileName}   => ${curFile?.index} / ${curFile?.fileSize}")

                    if (this.index < this.fileSize) {
                        sendCmd(Er1BleCmd.readFileData(this.index))
                    } else {
                        sendCmd(Er1BleCmd.readFileEnd())
                    }
                }
            }

            Er1BleCmd.ER1_CMD_READ_FILE_END -> {
                LogUtils.d("读文件完成: ${curFile?.fileName} ==> ${curFile?.fileSize}")
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
            if (temp.last() == Er1BleCRC.calCRC8(temp)) {
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

        linkLost = false
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
        if (reason == ConnectionObserver.REASON_LINK_LOSS) {
            linkLost = true
        }

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