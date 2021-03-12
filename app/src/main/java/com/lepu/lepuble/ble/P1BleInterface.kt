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
import com.lepu.lepuble.ble.cmd.P1BleCmd
import com.lepu.lepuble.ble.cmd.P1BleResponse
import com.lepu.lepuble.ble.obj.Er1DataController
import com.lepu.lepuble.ble.obj.LepuDevice
import com.lepu.lepuble.ble.utils.P1CRC
import com.lepu.lepuble.objs.Bluetooth
import com.lepu.lepuble.utils.add
import com.lepu.lepuble.utils.toHex
import com.lepu.lepuble.utils.toUInt
import com.lepu.lepuble.vals.EventMsgConst
import com.lepu.lepuble.viewmodel.Er1ViewModel
import com.lepu.lepuble.viewmodel.P1ViewModel
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import java.util.*
import kotlin.experimental.inv

class P1BleInterface : ConnectionObserver, P1BleManager.onNotifyListener {

    private lateinit var model: P1ViewModel
    fun setViewModel(viewModel: P1ViewModel) {
        this.model = viewModel
    }

    lateinit var manager: P1BleManager

    lateinit var mydevice: BluetoothDevice


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
        manager = P1BleManager(context)
        mydevice = device
        manager.setConnectionObserver(this)
        manager.setNotifyListener(this)
        manager.connect(device)
            .useAutoConnect(true)
            .timeout(10000)
            .retry(3, 100)
            .done {
                LogUtils.d("Device Init")

                /**
                 * the device may take 2 second to init the bluetooth.
                 * during th initing, no response
                 */
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        getSn()
                        updateState()
                    }}, 2000)
            }
            .enqueue()

    }

    public fun getSn() {
        sendCmd(P1BleCmd.getSn())
    }

    public fun updateState() {
        sendCmd(P1BleCmd.getState())
    }

    public fun turnOn(boolean: Boolean) {
        sendCmd(P1BleCmd.turnOn(boolean))
    }

    public fun changeMode(mode: Int) {
        sendCmd(P1BleCmd.setMode(mode))
    }

    public fun changeStrength(strength: Int) {
        sendCmd(P1BleCmd.setStrength(strength))
    }

    public fun setDuration(duration: Int) {
        sendCmd(P1BleCmd.setDuration(duration))
    }

    public fun setHeat(on: Boolean) {
        sendCmd(P1BleCmd.heatOn(on))
    }

    public fun disconnect() {
        manager.disconnect()
        manager.close()

        this.onDeviceDisconnected(mydevice, ConnectionObserver.REASON_SUCCESS)
    }


    private fun sendCmd(bs: ByteArray) {
        if (!state) {
            return
        }
        manager.sendCmd(bs)
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: P1BleResponse.P1Response) {
        LogUtils.d("Response received: ${response.cmd}  => ${response.content.toHex()}")
        when(response.cmd) {

            P1BleCmd.P1_CMD_SN -> {
                val sn = P1BleResponse.P1Sn(response.content)
                model.sn.value = sn.sn
            }

            P1BleCmd.P1_CMD_STATE -> {
                val state = P1BleResponse.P1State(response.content)
                model.power.value = state.power
                model.battery.value = state.battery
                model.heat.value = state.heating
                model.mode.value = state.mode
                model.strength.value = state.strength
                model.duration.value = state.duration
                LogUtils.d(state.toString())
            }
        }
    }

    @ExperimentalUnsignedTypes
    private fun hasResponse(bytes: ByteArray?){
        bytes?.apply {
            if (bytes.size < 9)
                return

            if (
                    (bytes[0] == 0x55.toByte()) and
                    (bytes[1] == 0xAA.toByte()) and
                    (bytes[bytes.size-1] == 0xFE.toByte())
            ){
                if (bytes[bytes.size-2] == P1CRC.CalCrc(bytes.copyOfRange(0, bytes.size-2))) {
                    val response = P1BleResponse.P1Response(bytes)
                    onResponseReceived(response)
                }
            }


        }
    }

    private fun clearVar() {
        model.battery.value = 0
        model.duration.value = 0
    }

    @ExperimentalUnsignedTypes
    override fun onNotify(device: BluetoothDevice?, data: Data?) {
        data?.value?.apply {
            hasResponse(this)
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
        LogUtils.d("${mydevice.name} ready")

    }
}