package com.lepu.lepuble.ble.obj

import android.os.Parcelable
import com.lepu.lepuble.utils.toUInt
import kotlinx.android.parcel.Parcelize

@ExperimentalUnsignedTypes
@Parcelize
class LepuDevice constructor(var bytes: ByteArray) : Parcelable {
    var hwV: Char? = null
    var fwV: String? = null
    var btlV: String? = null
    var branchCode: String? = null
    var fileV: Int? = null
    // reserve 2
    var deviceType: Int? = null
    var protocolV: String? = null
    var curTime: String? = null
    var protocolMaxLen: Int? = null
    // reserve 4
    var snLen: Int? = null
    var sn: String? = null


    // reserve 4

    init {
        hwV = bytes[0].toChar()
        fwV = "${bytes[4].toUInt()}.${bytes[3].toUInt()}.${bytes[2].toUInt()}.${bytes[1].toUInt()}"
        btlV = "${bytes[8].toUInt()}.${bytes[7].toUInt()}.${bytes[6].toUInt()}.${bytes[5].toUInt()}"
        branchCode = String(bytes.copyOfRange(9, 17))
        fileV = (bytes[17].toUInt() and 0xFFu).toInt()
        deviceType = toUInt(bytes.copyOfRange(20, 22))
        protocolV = "${bytes[22].toUInt()}.${bytes[23].toUInt()}"
        val year = toUInt(bytes.copyOfRange(24, 26))
        val month = (bytes[26].toUInt() and 0xFFu).toInt()
        val day = (bytes[27].toUInt() and 0xFFu).toInt()
        val hour = (bytes[28].toUInt() and 0xFFu).toInt()
        val min = (bytes[29].toUInt() and 0xFFu).toInt()
        val second = (bytes[30].toUInt() and 0xFFu).toInt()
        curTime = "$year/$month/$day $hour:$min:$second"
        protocolMaxLen = toUInt(bytes.copyOfRange(21, 23))
        snLen = (bytes[37].toUInt() and 0xFFu).toInt()
        sn = String(bytes.copyOfRange(38, 38+snLen!!))
    }

    override fun toString(): String {
        return """
            $deviceType: $fwV => $curTime
            $sn
        """.trimIndent()
    }
}