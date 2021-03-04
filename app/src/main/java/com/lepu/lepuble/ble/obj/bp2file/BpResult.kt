package com.lepu.lepuble.ble.obj.bp2file

import com.lepu.lepuble.utils.toUInt
import java.util.*

open class BpResult {

    var fileVersion: Int // 0x01 = v1
    var fileType: Int  // 1 bp, 2 ecg
    var time: Long
    var date: Date
    // reserve 4
    var statusCode: Int
    var sys : Int
    var dia : Int
    var mean : Int
    var pr : Int // pulse rate
    var diagnose : Int // 0: FIB
    // reserve 19

    var isRead : Boolean = false
    var isDelete : Boolean = false
    var note : String = ""
    var name : String = ""

    constructor(bytes : ByteArray) {
        this.fileVersion = bytes[0].toInt()
        this.fileType = bytes[1].toInt()
        this.time = toUInt(bytes.copyOfRange(2,6)).toLong()
        this.date = Date(this.time * 1000)
        this.statusCode = bytes[10].toInt()
        this.sys = toUInt(bytes.copyOfRange(11,13))
        this.dia = toUInt(bytes.copyOfRange(13,15))
        this.mean = toUInt(bytes.copyOfRange(15,17))
        this.pr = (bytes[17].toInt() and 0xff)
        this.diagnose = bytes[18].toInt()
    }
}