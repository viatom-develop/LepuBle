package com.lepu.lepuble.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Environment
import android.os.Handler
import androidx.annotation.NonNull
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.lepuble.ble.cmd.Er1BleCmd
import com.lepu.lepuble.ble.cmd.Er1BleResponse
import com.lepu.lepuble.ble.cmd.UniversalBleCmd
import com.lepu.lepuble.ble.obj.Er1DataController
import com.lepu.lepuble.ble.obj.LepuDevice
import com.lepu.lepuble.ble.utils.BleCRC
import com.lepu.lepuble.objs.Bluetooth
import com.lepu.lepuble.objs.SpeedTest
import com.lepu.lepuble.utils.HexString
import com.lepu.lepuble.utils.add
import com.lepu.lepuble.utils.toHex
import com.lepu.lepuble.utils.toUInt
import com.lepu.lepuble.vals.EventMsgConst
import com.lepu.lepuble.viewmodel.Er1ViewModel
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import kotlin.experimental.inv

class Er1BleInterface : ConnectionObserver, Er1BleManager.onNotifyListener {

    private lateinit var context: Context

    private lateinit var model: Er1ViewModel
    fun setViewModel(viewModel: Er1ViewModel) {
        this.model = viewModel
    }

    lateinit var manager: Er1BleManager

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

    inner class RtRriTask: Runnable {
        override fun run() {
            rtHandler.postDelayed(this, 100)
            if (state) {
                count++
                getRtRri()
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

        this.context = context

        if (connecting || state) {
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

        this.onDeviceDisconnected(mydevice, ConnectionObserver.REASON_SUCCESS)
    }

    /**
     * get device info
     */
    public fun getInfo() {
        sendCmd(UniversalBleCmd.getInfo())
    }

    /**
     * get real-time data
     */
    public fun getRtData() {
        sendCmd(UniversalBleCmd.getRtData())
    }

    /**
     * get real-time RRI
     */
    public fun getRtRri() {
        sendCmd(Er1BleCmd.getRtRri())
    }

    /**
     * run real-time task
     */
    public fun runRtTask() {
        rtHandler.postDelayed(RtTask(), 200)
    }

    /**
     * run real-time rri task
     */
    public fun runRtRriTask() {
        rtHandler.postDelayed(RtRriTask(), 200)
    }

    /**
     * get file list
     */
    public fun getFileList() {
        sendCmd(UniversalBleCmd.getFileList())
    }

    /**
     * 测试功能，下载"file_list.txt"文件
     * 每16个字节一个文件名
     */
    val ALL_FILE_NAME = "file_list.txt"
    private var isDownloadingAllFile = false
    val allFileList = mutableListOf<ByteArray>()
    public fun getAllFiles() {
        downloadFile(ALL_FILE_NAME.toByteArray())
    }

    private fun readAllFileList(bytes: ByteArray) {
        for (i in 0 until (bytes.size/16)) {
            allFileList.add(bytes.copyOfRange(i*16, (i+1)*16))
            totalFileNum++
        }
    }

    /**
     * save file
     */
    private fun saveFile(name: String, bytes: ByteArray?) {


        val file = File(context.filesDir, name)
        if (!file.exists()) {
            file.createNewFile()
        }


        try {
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(bytes)
            fileOutputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * download a file, name come from filelist
     */
    var curFileName: String? = null
    var curFile: Er1BleResponse.Er1File? = null
    var fileList: Er1BleResponse.Er1FileList? = null
    var fileNum: Int = 0
    var totalFileNum: Int = 0
    public fun downloadFile(name : ByteArray) {
        curFileName = HexString.trimStr(String(name))
        sendCmd(UniversalBleCmd.readFileStart(name, 0))
    }

    private fun sendCmd(bs: ByteArray) {
        if (!state) {
            return
        }
//        LogUtils.d(bs.toHex())
        manager.sendCmd(bs)
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: Er1BleResponse.Er1Response) {
//        LogUtils.d("received: ${response.cmd}")
        when(response.cmd) {
            UniversalBleCmd.GET_INFO -> {
                val erInfo = LepuDevice(response.content)
                model.er1.value = erInfo
                LiveEventBus.get(EventMsgConst.EventEr1Info)
                    .post(erInfo)

            }

            UniversalBleCmd.RT_DATA -> {
                val rtData = Er1BleResponse.RtData(response.content)
                model.hr.value = rtData.param.hr
                model.duration.value = rtData.param.recordTime
                model.lead.value = rtData.param.leadOn
                model.battery.value = rtData.param.battery

                Er1DataController.receive(rtData.wave.wFs)
//                LogUtils.d("ER1 Controller: ${Er1DataController.dataRec.size}")
                LiveEventBus.get(EventMsgConst.EventEr1RtData)
                    .post(rtData)
            }

            Er1BleCmd.RT_RRI -> {
                val rtRriData = Er1BleResponse.RtRriData(response.content)

                rtRriData.param.apply {
                    model.hr.value = this.hr
                    model.duration.value = this.recordTime
                    model.lead.value = this.leadOn
                    model.battery.value = this.battery

                    model.acceleration.value = """
                        x: ${this.axis_x} mg
                        y: ${this.axis_y} mg
                        z: ${this.axis_z} mg
                    """.trimIndent()

                }

                rtRriData.rri.apply {
                    model.rris.value = this.rris.toString()
                }
            }

            UniversalBleCmd.READ_FILE_LIST -> {
                fileList = Er1BleResponse.Er1FileList(response.content)
                LogUtils.d(fileList.toString())
            }

            UniversalBleCmd.READ_FILE_START -> {
                if (response.pkgType == 0x01.toByte()) {
                    curFile = Er1BleResponse.Er1File(curFileName!!, toUInt(response.content))
                    sendCmd(UniversalBleCmd.readFileData(0))

                    // speed test
                    SpeedTest.init()
                } else {
                    LogUtils.d("read file failed：${response.pkgType}")
                    proceedNextFile()
                }

            }

            UniversalBleCmd.READ_FILE_DATA -> {
                curFile?.apply {
                    this.addContent(response.content)
//                    LogUtils.d("read file：${curFile?.fileName}   => ${curFile?.index} / ${curFile?.fileSize}")

                    // speed test
                    val s = SpeedTest.add(response.content.size)
                    model.speed.postValue(s)
//                    LogUtils.d("speed in 10s is $s")

                    if (this.index < this.fileSize) {
                        sendCmd(UniversalBleCmd.readFileData(this.index))
                    } else {
                        sendCmd(UniversalBleCmd.readFileEnd())
                    }
                }
            }

            UniversalBleCmd.READ_FILE_END -> {
                LogUtils.d("read file finished: ${curFile?.fileName} ==> ${curFile?.fileSize}")

                saveFile(curFile!!.fileName, curFile!!.content)

                if (curFile?.fileName == ALL_FILE_NAME) {
                    isDownloadingAllFile = true
                    readAllFileList(curFile!!.content)
                }

                curFileName = null
                curFile = null

                proceedNextFile()
            }
        }
    }

    private fun proceedNextFile() {

        if (isDownloadingAllFile) {
            allFileList.removeAt(0)
            fileNum++
            LiveEventBus.get(EventMsgConst.EventCommonMsg).post("$fileNum/$totalFileNum")

            if (allFileList.size > 0) {
                downloadFile(allFileList[0])
            } else {
                isDownloadingAllFile = false
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
        rtHandler.removeCallbacks(RtRriTask())

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