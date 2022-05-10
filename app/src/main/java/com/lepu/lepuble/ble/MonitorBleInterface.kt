package com.lepu.lepuble.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.annotation.NonNull
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.lepuble.ble.cmd.MonitorRtData
import com.lepu.lepuble.ble.cmd.OxyBleResponse
import com.lepu.lepuble.ble.utils.BleCRC
import com.lepu.lepuble.objs.Bluetooth
import com.lepu.lepuble.utils.byteArrayOfInts
import com.lepu.lepuble.utils.toHex
import com.lepu.lepuble.utils.toUInt
import com.lepu.lepuble.vals.EventMsgConst
import com.lepu.lepuble.viewmodel.MainViewModel
import com.lepu.lepuble.viewmodel.MonitorViewModel
import com.lepu.lepuble.viewmodel.OxyViewModel
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import kotlin.experimental.inv

class MonitorBleInterface : ConnectionObserver, MonitorBleManager.onNotifyListener {
    private var mainVM: MainViewModel? = null
    fun setMainVM(model: MainViewModel) {
        mainVM = model
    }

    private lateinit var model: MonitorViewModel
    fun setViewModel(viewModel: MonitorViewModel) {
        this.model = viewModel
    }

    lateinit var manager: MonitorBleManager
    lateinit var mydevice: BluetoothDevice


    public var state = false
    private var connecting = false

    public fun connect(context: Context, @NonNull device: BluetoothDevice) {
        if (connecting || state) {
            return
        }
        LogUtils.d("try connect: ${device.name}")
        manager = MonitorBleManager(context)
        mydevice = device
        manager.connectionObserver = this
        manager.setNotifyListener(this)
        manager.connect(device)
            .useAutoConnect(true)
            .timeout(10000)
            .retry(3, 100)
            .done {
                LogUtils.d("Device Init")
                getRtData()
            }
            .enqueue()

    }

    /**
     * 用于测试
     */
    public fun sendCmd(bs: ByteArray) {
        manager.sendCmd(bs)
    }

    private fun clearVar() {
//        model.battery.value = 0
        model.pr.value = 0
        model.spo2.value = 0
//        model.pi.value = 0.0f
    }

    fun disconnect() {
        manager.disconnect()
        manager.close()

        this.onDeviceDisconnected(mydevice, ConnectionObserver.REASON_SUCCESS)
    }


    fun getRtData() {
        sendCmd(byteArrayOfInts(0))
    }

    /**
     * Checkme Pro will send one package every 40ms
     * contains 5 ECG dots and 5 SpO2 dots
     */
    override fun onNotify(device: BluetoothDevice?, data: Data?) {
        data?.apply {
//            LogUtils.d(data)
            val d = MonitorRtData(this.value)
            LiveEventBus.get(EventMsgConst.EventMonitorRtdata).post(d)
            LogUtils.d("${d.hr}, ${d.spo2}, ${d.battery}")
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

        connecting = false
    }
}