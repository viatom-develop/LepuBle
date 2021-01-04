package com.lepu.lepuble.ble.obj

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

object KcaBpConfig {

    @Parcelize
    class MeasureConfig constructor(var bytes: ByteArray?): Parcelable {
        var mode: Int // 1: 手动 2自动 3序列
        var dayStH: Int
        var dayStM: Int
        var dayEdH: Int
        var dayEdM: Int
        var dayInt: Int
        var nightStH: Int
        var nightStM: Int
        var nightEdH: Int
        var nightEdM: Int
        var nightInt: Int


        /**
         * 1byte 模式 :1: 手动 2自动 3序列
         * 1byte 手动模式参数
         * 1byte 自动模式参数
         * 白天开始时间2byte-白天结束时间2byte+ 采集间隔2byte
         * 白天开始时间2byte-白天结束时间2byte+ 采集间隔2byte
         */
        init {

            mode = 2
            dayStH = 6
            dayStM = 0
            dayEdH = 22
            dayEdM = 0
            nightStH = 22
            nightStM = 0
            nightEdH = 6
            nightEdM = 0
            dayInt = 30
            nightInt = 60

            bytes?.apply {
                mode = this[0].toInt()
                if (mode == 2) {
                    dayStH = this[1].toInt()
                    dayStM = this[2].toInt()
                    dayEdH = this[3].toInt()
                    dayEdM = this[4].toInt()
                    dayInt = ((this[5].toInt() * 60) + this[6].toInt())
                    nightStH = this[7].toInt()
                    nightStM = this[8].toInt()
                    nightEdH = this[9].toInt()
                    nightEdM = this[10].toInt()
                    nightInt = ((this[11].toInt() * 60) + this[12].toInt())
                }
            }
        }

        override fun toString(): String {
            return """
                定时测量配置
                $dayStH:$dayStM - $dayEdH:$dayEdM, $dayInt
                $nightStH:$nightStM - $nightEdH:$nightEdM, $nightInt
            """.trimIndent()
        }
    }
}