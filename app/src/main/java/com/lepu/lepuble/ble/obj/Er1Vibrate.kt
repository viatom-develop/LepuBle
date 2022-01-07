package com.lepu.lepuble.ble.obj

import com.google.gson.Gson

class Er1Vibrate {
    var on: Boolean = false
    var threshold1 : Int = 0
    var threshold2 : Int = 0

    constructor(on : Boolean,threshold1: Int, threshold2: Int) {
        this.on = on
        this.threshold1 = threshold1
        this.threshold2 = threshold2
    }

    constructor(bytes: ByteArray) {
        if (bytes.size != 3) {
            return
        }
        this.on = bytes[0] == 1.toByte()
        this.threshold1 = bytes[1].toUInt().toInt()
        this.threshold2 = bytes[2].toUInt().toInt()
    }

    public fun toBytes(): ByteArray {
        val tmp = ByteArray(3)

        tmp[0] = if (on) {
            1.toByte()
        } else {
            0.toByte()
        }

        tmp[1] = threshold1.toByte()
        tmp[2] = threshold2.toByte()

        return tmp
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }
}