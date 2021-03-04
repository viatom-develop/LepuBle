package com.lepu.lepuble.ble.obj.bp2file

import com.lepu.lepuble.utils.toUInt

class EcgResult {
    var fileVersion: Int // 0x01 = v1
    var fileType: Int  // 1 bp, 2 ecg
    var time: Long
    // reserve 4
    var duration : Int
    // reserve 2
    var diagnose : ByteArray?
    var hr : Int
    var qrs : Int
    var pvcs : Int
    var qtc : Int
    // for bp
    var bpConnectMode: Int
    // reserve 20
    var wave : ByteArray?

    constructor(bytes : ByteArray) {
        this.fileVersion = bytes[0].toInt()
        this.fileType = bytes[1].toInt()
        this.time = toUInt(bytes.copyOfRange(2,6)).toLong()
        this.duration = toUInt(bytes.copyOfRange(10, 14))
        this.diagnose = bytes.copyOfRange(16, 20)
        this.hr = toUInt(bytes.copyOfRange(20, 22))
        this.qrs = toUInt(bytes.copyOfRange(22, 24))
        this.pvcs = toUInt(bytes.copyOfRange(24, 26))
        this.qtc = toUInt(bytes.copyOfRange(26, 28))
        this.bpConnectMode = bytes[28].toInt()

        this.wave = bytes.copyOfRange(48, bytes.size)
    }

    fun getEcgDiagnosis(): EcgDiagnosis {
        return EcgDiagnosis(diagnose)
    }

}