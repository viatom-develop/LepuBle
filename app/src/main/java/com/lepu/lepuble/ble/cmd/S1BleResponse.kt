package com.lepu.lepuble.ble.cmd

import android.os.Parcelable
import com.blankj.utilcode.util.LogUtils
import com.lepu.lepuble.ble.obj.Er1DataController
import com.lepu.lepuble.utils.ByteUtils
import com.lepu.lepuble.utils.ByteUtils.bytes2UIntBig
import com.lepu.lepuble.utils.toHex
import com.lepu.lepuble.utils.toUInt
import kotlinx.android.parcel.Parcelize
import kotlin.experimental.and
import kotlin.math.pow

class S1BleResponse {

    @Parcelize
    class RtWave @ExperimentalUnsignedTypes constructor(var bytes: ByteArray) : Parcelable {
        var content: ByteArray = bytes
        var len: Int
        var wave: ByteArray
        var wFs : FloatArray? = null

        init {
            len = toUInt(bytes.copyOfRange(0, 2))
            wave = bytes.copyOfRange(2, bytes.size)
            wFs = FloatArray(len)
            for (i in 0 until len) {
                wFs!![i] = Er1DataController.byteTomV(wave[2 * i], wave[2 * i + 1])
            }


            LogUtils.d(len)
        }
    }

    class S1ScaleData constructor(var bytes: ByteArray) {
        var subtype: Int//固定 0x1A
        var vendor : Int//固定0x41

        /**
         * 测量标识
         * 1. 对于体脂称： 纯体重数据： 0xA0， 带阻值数据： 0xAA
         * 2. 对于体重秤： 实时数据： 0xB0， 定格数据： 0xBB（根据
         *    不同测量阶段修改，当数据从定格数据变为实时数据视为重新上秤测量）
         */
        var mask: Int

        /**
         * bit0-3:单位 0:kg, 1:LB, 2:ST, 3:LB-ST, 4:斤
         * bit4-7:精度 表示后面的重量被放大了10^n
         */
        var unit: String
        var weight: Double
        var amp: Int
        var resistance: Int
        var crc: Int

        init {
            LogUtils.d(bytes.toHex())
            var index = 0
            subtype = bytes[index].toInt()
            index++
            vendor = bytes[index].toInt()
            index++
            mask = bytes[index].toInt()
            index++
            unit = ""
            when(bytes[index] and 0x0f) {
                0x00.toByte() -> unit = "kg"
                0x01.toByte() -> unit = "lb"
                0x02.toByte() -> unit = "st"
                0x03.toByte() -> unit = "lb-st"
                0x04.toByte() -> unit = "斤"
            }
            amp = ((bytes[index] and 0xf0.toByte()).toInt() shr 4)
            index++
            weight = (ByteUtils.bytes2UIntBig(bytes[index], bytes[index+1]) / (10.0.pow(amp.toDouble())))
            index+=2
            resistance = bytes2UIntBig(bytes[index], bytes[index+1], bytes[index+2], bytes[index+3])
            index+=4
            crc = bytes[index].toInt()

            LogUtils.d(mask, weight, amp, unit, resistance)
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class RtParam constructor(var bytes: ByteArray) : Parcelable {
        var status: Int // //运行状态  0:待机 1:秤端测量中 2:秤端测量结束 3:心电准备阶段 4:心电测量中 5:心电正常结束 6:带阻抗心电异常结束 7:不带阻抗异常结束
        var hr: Int
        var recordTime: Int = 0
        var leadOn: Boolean
        var bleStatus: Int
        // reserve 7

        init {
            LogUtils.d(bytes.toHex())
            var index = 0
            status = bytes[index].toInt()
            index++
            hr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            recordTime = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            leadOn = bytes[index] != 0x00.toByte()
            index++
            bleStatus = bytes[index].toInt()
            index++

            index += 7 // reserve
            LogUtils.d(hr, leadOn)
        }
    }

    class S1RtData constructor(var bytes: ByteArray) {
        var param: RtParam
        var scaleData: S1ScaleData
        var wave: RtWave

        init {
            var index = 0
            param = RtParam(bytes.copyOfRange(index, index+16))
            index += 16
            scaleData = S1ScaleData(bytes.copyOfRange(index, index+11))
            index += 11
            wave = RtWave(bytes.copyOfRange(index, bytes.size))
        }
    }
}