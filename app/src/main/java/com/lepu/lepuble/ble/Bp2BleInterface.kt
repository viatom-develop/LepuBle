package com.lepu.lepuble.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import androidx.annotation.NonNull
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.lepuble.ble.cmd.*
import com.lepu.lepuble.ble.obj.Er1DataController
import com.lepu.lepuble.ble.obj.LepuDevice
import com.lepu.lepuble.ble.utils.BleCRC
import com.lepu.lepuble.file.BP2File
import com.lepu.lepuble.file.Er2Record
import com.lepu.lepuble.objs.Bluetooth
import com.lepu.lepuble.utils.HexString
import com.lepu.lepuble.utils.add
import com.lepu.lepuble.utils.toUInt
import com.lepu.lepuble.vals.EventMsgConst
import com.lepu.lepuble.viewmodel.Bp2ViewModel
import com.lepu.lepuble.viewmodel.MainViewModel
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import kotlin.experimental.inv

class Bp2BleInterface : ConnectionObserver, LepuBleManager.onNotifyListener {

    private lateinit var context: Context

    private lateinit var model: Bp2ViewModel
    fun setViewModel(viewModel: Bp2ViewModel) {
        this.model = viewModel
    }

    lateinit var manager: LepuBleManager

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

        this.context = context
        LogUtils.d("try connect: ${device.name}")
        manager = LepuBleManager(context)
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
        sendCmd(Bp2BleCmd.getRtData())
    }

    /**
     * run real-time task
     */
    public fun runRtTask() {
        rtHandler.postDelayed(RtTask(), 200)
    }

    /**
     * all files on the device, use for download all
     */
    private val allFileList = mutableListOf<ByteArray>()

    /**
     * get file list
     */
    public fun getFileList() {
        sendCmd(UniversalBleCmd.getFileList())
    }

    private fun processFileList(list: Er1BleResponse.Er1FileList) {
        for (name in list.fileList) {
            if (HexString.trimStr(String(name)).startsWith("MKFS")) {
                continue
            }
            allFileList.add(name)
            totalFileNum++
        }
    }

    private var isDownloadingAllFile = false

    /**
     * download a file, name come from filelist
     */
    var curFileName: String? = null
    var curFile: Er1BleResponse.Er1File? = null
    var fileNum: Int = 0
    var totalFileNum: Int = 0

    /**
     * download file from the device
     */
    public fun downloadFile(name : ByteArray) {
        curFileName = String(name)
        sendCmd(UniversalBleCmd.readFileStart(name, 0))
    }

    /**
     * save file to local storage
     */
    private fun saveFile(name: String, bytes: ByteArray?) {

        val file = File(context.filesDir, HexString.trimStr(name))
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


    public fun sendCmd(bs: ByteArray) {
        if (!state) {
            return
        }
        manager.sendCmd(bs)
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: UniversalBleResponse.LepuResponse) {
//        LogUtils.d("received: ${response.cmd}")
        when(response.cmd) {
            UniversalBleCmd.GET_INFO -> {
                val info = LepuDevice(response.content)
                model.bp2.value = info
                LogUtils.d("device info: $info")
            }

            Bp2BleCmd.RT_DATA -> {
                val rtData = Bp2Response.RtData(response.content)
//                model.hr.value = rtData.param.hr
//                model.duration.value = rtData.param.recordTime
//                model.lead.value = rtData.param.leadOn
                model.battery.value = rtData.param.batteryLevel
                model.status.value = rtData.param.status
                val wave = rtData.wave
                wave.dataBping?.apply {
                    model.pr.value = this.pr
                    model.sys.value = this.pressure / 100
                    model.dia.value = 0
                    model.mean.value = 0
                }
                wave.dataBpResult?.apply {
                    model.pr.value = this.pr
                    model.sys.value = this.sys
                    model.dia.value = this.dia
                    model.mean.value = this.mean
                }
                wave.dataEcging?.apply {
                    model.hr.value = this.hr
                    model.duration.value = this.duration
                    Er1DataController.receive(wave.waveFs)
                }
                wave.dataEcgResult?.apply {
                    model.hr.value = this.hr
                }

//                Er1DataController.receive(rtData.wave.wFs)
                LogUtils.d("${rtData.toString()}")
                LiveEventBus.get(EventMsgConst.EventEr1RtData)
                    .post(rtData)
            }

            Bp2BleCmd.RT_PARAM -> {
                val param = Bp2Response.RtParam(response.content)
                model.battery.value = param.batteryLevel
                model.status.value = param.status
            }

            Bp2BleCmd.RT_WAVE -> {
                val wave = Bp2Response.RtWave(response.content)
                LogUtils.d("$wave")
            }

            UniversalBleCmd.READ_FILE_LIST -> {
                val fileList = Er1BleResponse.Er1FileList(response.content)
                processFileList(fileList)
                LogUtils.d(fileList.toString())

                // download all files
                isDownloadingAllFile = true
                proceedNextFile()
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

                saveFile(curFile!!.fileName, curFile!!.content)

                val bP2File = BP2File(curFile!!.content)
                    LogUtils.d(bP2File)
//                LogUtils.d(bP2File.toAIFile())

                curFileName = null
                curFile = null

                proceedNextFile()
            }
        }
    }

    private fun proceedNextFile() {
        if (isDownloadingAllFile) {
            fileNum++
            LiveEventBus.get(EventMsgConst.EventCommonMsg).post("$fileNum/$totalFileNum")

            if (allFileList.size > 0) {
                downloadFile(allFileList[0])
                allFileList.removeAt(0)
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
                val bleResponse = UniversalBleResponse.LepuResponse(temp)
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
    }

//    override fun onResponse(response: UniversalBleResponse.LepuResponse?) {
//        response?.apply {
//            onResponseReceived(this)
//        }
//    }
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

    @SuppressLint("MissingPermission")
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

    @SuppressLint("MissingPermission")
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