package com.lepu.lepuble.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.annotation.NonNull
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.lepuble.ble.cmd.Am300Obj
import com.lepu.lepuble.ble.cmd.Am300bBleCmd
import com.lepu.lepuble.ble.cmd.Am300bBleCmd.*
import com.lepu.lepuble.ble.cmd.Er1BleResponse
import com.lepu.lepuble.ble.utils.Am300bCRC
import com.lepu.lepuble.ble.utils.BleCRC
import com.lepu.lepuble.objs.Bluetooth
import com.lepu.lepuble.utils.ByteUtils
import com.lepu.lepuble.utils.add
import com.lepu.lepuble.utils.toHex
import com.lepu.lepuble.utils.toUInt
import com.lepu.lepuble.vals.EventMsgConst
import com.lepu.lepuble.viewmodel.Am300bViewModel
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import java.util.*
import kotlin.concurrent.schedule
import kotlin.experimental.inv

class Am300bBleInterface : ConnectionObserver, Am300bBleManager.onNotifyListener {

    private lateinit var context: Context

    private lateinit var model: Am300bViewModel
    fun setViewModel(viewModel: Am300bViewModel) {
        this.model = viewModel
    }

    lateinit var manager: Am300bBleManager

    lateinit var mydevice: BluetoothDevice

    private var pool: ByteArray? = null

    public var state = false
    private var connecting = false

    public fun connect(context: Context, @NonNull device: BluetoothDevice) {

        this.context = context

        if (connecting || state) {
            return
        }
        LogUtils.d("try connect: ${device.name}")
        manager = Am300bBleManager(context)
        mydevice = device
        manager.setConnectionObserver(this)
        manager.setNotifyListener(this)
        manager.connect(device)
            .useAutoConnect(true)
            .timeout(10000)
            .retry(3, 100)
            .done {
                LogUtils.d("Device Init")
                Timer().schedule(3000) {
                    getSn()
                    getVersion()
//                    getBattery()
                }
            }
            .enqueue()

    }

    public fun disconnect() {
        manager.disconnect()
        manager.close()

        this.onDeviceDisconnected(mydevice, ConnectionObserver.REASON_SUCCESS)
    }


    /*----------------------------------*/
    public fun getSn() {
        sendCmd(Am300bBleCmd.getSn())
    }

    public fun getVersion() {
        sendCmd(Am300bBleCmd.getVersion())
    }

    public fun getBattery() {
        sendCmd(Am300bBleCmd.getBattery())
    }

    public fun startEmg() {
        sendCmd(Am300bBleCmd.EmgStart())
    }

    public fun endEmg() {
        sendCmd(Am300bBleCmd.EmgEnd())
    }

    public fun setParam(
        channel: Int,
        freq: Int,
        bandwidth : Int,
        raise: Float,
        fall: Float,
        duration: Int,
        rest: Int
    ) {
        sendCmd(Am300bBleCmd.setIntensityParam(channel, freq, bandwidth, raise, fall, duration, rest))
    }

    public fun setIntensity(value: Int, channel: Int) {
        sendCmd(Am300bBleCmd.setIntensity(value, channel))
    }

    public fun startIntensity(channel: Int) {
        sendCmd(Am300bBleCmd.intensityStart(channel))
    }

    public fun endIntensity(channel: Int) {
        sendCmd(Am300bBleCmd.intensityEnd(channel))
    }


    /*----------------------------------*/

    private fun sendCmd(bs: ByteArray) {
        if (!state) {
            return
        }
        LogUtils.d("send cmd: ${bs.toHex()}")
        manager.sendCmd(bs)
    }

    private fun onResponseReceived(response: Am300bBleCmd.BleCmd) {

        when(ByteUtils.byte2UInt(response.token)) {
            TOKEN_UNIVERSAL -> {
                when(ByteUtils.byte2UInt(response.cmd)) {
                    ACK_VERSION -> {
//                        LogUtils.d(response.toBytes().toHex())
                        model.version.value = "V${Am300Obj.Version(response.content).sf_version}"
                    }
                    ACK_SN -> {
//                        LogUtils.d(response.toBytes().toHex())
                        model.sn.value = "SN: ${Am300Obj.SN(response.content).sn}"
                    }
                    ACK_BATTERY -> {
                        val b = Am300Obj.Battery(response.content)
                        model.battery.value = b.level
//                        LogUtils.d("battery: ${b.level}%")
                    }
                }
            }
            TOKEN_KF -> {
//                LogUtils.d(response.toBytes().toHex())
                when(ByteUtils.byte2UInt(response.cmd)) {
                    ACK_EMG_START -> {
                        val o = Am300Obj.EmgStart(response.content)
                        LogUtils.d(o)
                        if (o.isSuccess) {
                            model.emgState.value = true
                        }
                    }
                    ACK_EMG_END -> {
                        val o = Am300Obj.EmgEnd(response.content)
                        LogUtils.d(o)
                        if (o.isSuccess) {
                            model.emgState.value = false
                        }
                    }
                    ACK_EMG_PKG -> {
                        val o = Am300Obj.EmgPkg(response.content)
//                        LogUtils.d(o)
                        model.emgPkg.value = o
                    }
                    ACK_EMG_LEAD -> {
                        val o = Am300Obj.EmgLeadOff(response.content)
//                        LogUtils.d(o)
                        model.emgLead.value = o
                    }
                    ACK_STIMULATE_CONFIG -> {
                        LogUtils.d(response.toBytes().toHex())
                    }
                    ACK_STIMULATE_CONFIG_QUERY -> {}
                    ACK_INTENSITY_CONFIG -> {
                        LogUtils.d(response.toBytes().toHex())
                    }
                    ACK_INTENSITY_QUERY -> {}
                    ACK_STIMULATE_START -> {
                        LogUtils.d(response.toBytes().toHex())
                    }
                    ACK_STIMULATE_END -> {
                        LogUtils.d(response.toBytes().toHex())
                    }
                    ACK_STIMULATE_PAUSE -> {}
                    ACK_STIMULATE_PKG -> {}
                    ACK_BATTERY_LOW -> {}
                }
            }
        }
    }

    override fun onNotify(device: BluetoothDevice?, data: Data?) {
        data?.value?.apply {
//            LogUtils.d("onNotify: ${this.toHex()}")
            pool = add(pool, this)
        }
        pool?.apply {
            pool = hasResponse(pool)
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-7) {
            if (bytes[i] != 0xAA.toByte() || bytes[i+1] != 0x55.toByte()) {
                continue@loop
            }

            // need content length
            val len = toUInt(bytes.copyOfRange(i+3, i+4))
//            Log.d(TAG, "want bytes length: $len")
            if (i+4+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+4+len)
            if (temp.last() == Am300bCRC.calCRC8(temp)) {
                val bleResponse = Am300bBleCmd.BleCmd(temp)
//                LogUtils.d("get response: ${temp.toHex()}" )
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i+4+len == bytes.size) null else bytes.copyOfRange(i+4+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

//    private fun onResponseReceived()

    override fun onDeviceConnected(device: BluetoothDevice) {
        state = true
        model.connect.value = state
        LogUtils.d("onDeviceConnected: ${mydevice.name}")

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