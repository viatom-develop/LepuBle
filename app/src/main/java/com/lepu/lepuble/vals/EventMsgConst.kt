package com.lepu.lepuble.vals

import java.util.*

object EventMsgConst {

    /**
     * ble discovery
     */
    const val EventDeviceFound = "com.lepu.ble.device.found"
    const val EventDeviceChoosen = "com.lepu.ble.device.choosen"
    const val EventDeviceDisconnect = "com.lepu.ble.device.disconnect"

    /**
     * ER1 LiveDataBus Event
     */
    const val EventEr1Info = "com.lepu.ble.er1.info"
    const val EventEr1RtData = "com.lepu.ble.er1.rtData"
    const val EventEr1InvalidRtData = "com.lepu.ble.er1.invalid.rtData"
    const val EventEr1Unbind = "com.lepu.ble.er1.unbind"

    /**
     * Oxy LiveDataBus Event
     */
    const val EventOxyInfo = "com.lepu.ble.oxy.info"
    const val EventOxyRtData = "com.lepu.ble.oxy.rtData"
    const val EventOxyInvalidRtData = "com.lepu.ble.oxy.invalid.rtData"
    const val EventOxyUnbind = "com.lepu.ble.oxy.unbind"

    /**
     * KcaBle LiveDataBus event
     */
    const val EventKcaSn = "com.lepu.ble.kac.sn"
    const val EventKcaMeasureState = "com.lepu.ble.kac.measure.state"
    const val EventKcaBpResult = "com.lepu.ble.kac.bp.result"
    const val EventKcaUnbind = "com.lepu.ble.kac.unbind"
    const val EventKcaBpConfig = "com.lepu.ble.kac.bp.config"


    /**
     * bind new device LiveDataBus
     */
    const val EventBindEr1Device = "com.lepu.ble.bind.device.er1"
    const val EventBindO2Device = "com.lepu.ble.bind.device.o2"
    const val EventBindKcaDevice = "com.lepu.ble.bind.device.kca"


    /**
     * socket
     */
    const val EventSocketConnect = "com.lepu.socket.connect"
    const val EventSocketMsg = "com.lepu.socket.msg"

    /**
     * just send a message
     */
    const val EventCommonMsg = "com.lepu.common.message"
    const val EventBleLog = "com.lepu.ble.log"
    const val EventBlePkg = "com.lepu.ble.package"
    const val EventMsgSendCmd = "com.lepu.ble.log.send"
    const val EventDialogMsg = "com.lepu.ble.dialog.msg"

    /**
     * ER3 live event bus
     */
    const val EventEr3GetConfig = "com.lepu.ble.er3.get.config"

    /**
     * Monitor liveevent data
     *
     */
    const val EventMonitorRtdata = "com.lepu.ble.monitor.rt.data"
}