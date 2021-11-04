package com.lepu.lepuble.ble.cmd

import com.blankj.utilcode.util.LogUtils
import com.google.gson.Gson
import com.lepu.lepuble.utils.HexString
import com.lepu.lepuble.utils.toHex
import com.lepu.lepuble.utils.toUInt
import kotlin.experimental.and

object Am300Obj {

    class Battery(var bytes: ByteArray) {
        var level: Int
        val voltage: Int

        init {
            level = bytes[0]*25
            voltage = bytes[1]*256 + bytes[2]
        }
        override fun toString(): String {
            return Gson().toJson(this)
        }
    }

    class SN(var bytes: ByteArray) {
        var sn: String
        init {
            sn = HexString.trimStr(String(bytes))
        }

        override fun toString(): String {
            return Gson().toJson(this)
        }
    }

    class Version(var bytes: ByteArray) {
        var sf_version: String
        var hd_version: String
        var name: String

        init {
            var index = 0
            sf_version = bytes.copyOfRange(index, index+2).toHex()
            index += 2
            hd_version = String(bytes.copyOfRange(index, index+1))
            index++
            name = HexString.trimStr(String(bytes.copyOfRange(index, bytes.size)))

        }

        override fun toString(): String {
            return Gson().toJson(this)
        }
    }

    class EmgStart(var bytes: ByteArray) {
        var isSuccess: Boolean

        init {
            isSuccess = bytes[0] == 0x00.toByte()
        }

        override fun toString(): String {
            return """
                isSuccess: $isSuccess
                ${bytes.toHex()}
            """.trimIndent()
        }
    }
    class EmgEnd(var bytes: ByteArray) {
        var isSuccess: Boolean

        init {
            isSuccess = bytes[0] == 0x00.toByte()
        }

        override fun toString(): String {
            return """
                isSuccess: $isSuccess
                ${bytes.toHex()}
            """.trimIndent()
        }
    }

    class EmgPkg(var bytes: ByteArray) {
        val data: Int
        val a: Int
        val b: Int

        init {
            data = toUInt(bytes)
            a = toUInt(bytes.copyOfRange(0,2))
            b = toUInt(bytes.copyOfRange(2,4))
        }

        override fun toString(): String {
            return Gson().toJson(this)
        }
    }
    class EmgLeadState(var bytes: ByteArray) {

        /**
         * Probe_lead:探头是否脱落。 = 1,脱落； =0不脱落字节不动用位区分
        electrode_lead：参考电极片是否脱落 --= 1,脱落； =0不脱落

         */
        var probe_lead: Boolean
        var electrode_lead: Boolean

        var probeA: Boolean
        var probeB: Boolean
        var electrode1: Boolean

        init {
//            LogUtils.d(bytes.toHex())
            probe_lead = bytes[0] == 0x00.toByte()
            electrode_lead = bytes[1] == 0x00.toByte()

            probeA = (bytes[0] and 0x01) != 0x01.toByte()
            probeB = (bytes[0] and 0x02) != 0x02.toByte()

            electrode1 = (bytes[1] and 0x01) != 0x01.toByte()
        }

        override fun toString(): String {
            return Gson().toJson(this)
        }
    }

    class Stimulate {
        public var freq : Int // 1~120, step 1 频率
        public var bandwith : Int // 50~450. step 50  脉宽
        public var raise : Float // 0~18, step 0.1 上升时间
        public var fall : Float // 0~18, step 0.1 下降时间
        public var duration: Int // 0~60, step 1 刺激时间
        public var reset: Int // 0~60, step 1 休息时间

        constructor(bytes: ByteArray) {
            var index = 0
            freq = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            bandwith = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            raise = bytes[index].toInt() / 10f
            index++
            duration = bytes[index].toInt()
            index++
            fall = bytes[index].toInt() / 10f
            index++
            reset = bytes[index].toInt()
        }

        constructor(
            freq: Int = 1,
            bandwith: Int = 50,
            raise : Float = 0.0f,
            fall :Float = 0.0f,
            duration: Int = 0,
            reset: Int = 0
        ) {
            this.freq = freq
            this.bandwith = bandwith
            this.raise = raise
            this.fall = fall
            this.duration = duration
            this.reset = reset
        }

        public fun toBytes(): ByteArray {
            val c = listOf<Byte>((freq shr 8).toByte(), freq.toByte(), (bandwith shr 8).toByte(), bandwith.toByte(), (raise*10).toInt().toByte(), duration.toByte(), (fall*10).toInt().toByte(), reset.toByte()
            )
            return c.toByteArray()
        }

        override fun toString(): String {
            return Gson().toJson(this)
        }
    }
}