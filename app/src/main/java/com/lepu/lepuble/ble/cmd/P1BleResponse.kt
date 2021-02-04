package com.lepu.lepuble.ble.cmd

import com.lepu.lepuble.utils.ByteUtils
import com.lepu.lepuble.utils.HexString
import com.lepu.lepuble.utils.toHex
import com.lepu.lepuble.utils.toUInt

object P1BleResponse {

    @ExperimentalUnsignedTypes
    class P1Response constructor(var bytes: ByteArray) {

        var cmd: Int
        var seq: Int
        var content: ByteArray

        init {
            // 0x55 AA
            seq = ((bytes[2].toUInt().toInt() and 0xff) shr 8) + (bytes[3].toUInt().toInt())
            cmd = toUInt(bytes.copyOfRange(5, 6))
            content = bytes.copyOfRange(7, bytes.size-2)
        }
    }

    class P1State constructor(var bytes: ByteArray) {
        var power: Boolean
        var battery: Int
        var heating: Boolean
        var mode: Int
        var strength: Int
        var duration: Int

        init {
            power = bytes[0] == 0x01.toByte()
            battery = bytes[1].toInt()
            heating = bytes[2] == 0x01.toByte()
            mode = bytes[3].toInt()
            strength = bytes[4].toInt()
            duration = bytes[5].toInt()
        }

        override fun toString(): String {
            return """
                on/off: $power
                battery: $battery
                heating: $heating
                mode: $mode
                strength: $strength
                duration: $duration
            """.trimIndent()
        }
    }

    class P1Sn constructor(var bytes: ByteArray) {
        var year: Int
        var month: Int
        var model: Int
        var serial: Int
        var sn: String

        init {
            year = 2000 + ByteUtils.byte2UInt(bytes[0])
            month = ByteUtils.byte2UInt(bytes[1])
            model = ByteUtils.byte2UInt(bytes[2])
            serial = ByteUtils.bytes2UIntBig(bytes[3], bytes[4])
            sn = HexString.trimStr(bytes.toHex())
        }
    }
}