package com.lepu.lepuble.file

import com.google.gson.Gson
import com.lepu.lepuble.utils.toUInt
import java.util.*

class OxyDataFile {
    private var version: Int
    private var mode: Int //0: sleep; 1:monitor
    private var startTime: Date
    private var size: Int

    // result
    private var recordTime: Int // total record time, in second
    private var asleepTime: Int // reserved
    private var avgSpo2: Int // average SpO2
    private var minSpo2: Int // min SpO2
    private var drop3: Int // drop times below 3%
    private var drop4: Int // drop times below 4%
    private var druation90: Int // <90% duration
    private var drops90: Int // <90% times
    private var percent90: Float // <90% percent
    private var score: Float // O2 Score, -0.1: invalid value
    private var steps: Int
    private var spo2Wave: MutableList<O2Sample> = mutableListOf()


    @OptIn(ExperimentalUnsignedTypes::class)
    constructor(bytes: ByteArray) {
        var index = 0
        version = bytes[index].toInt()
        index++
        mode = bytes[index].toInt()
        index++
        // time
        val c = Calendar.getInstance()
        val year = toUInt(bytes.copyOfRange(index, index+2))
        val month = bytes[index+2].toInt()-1
        val day = bytes[index+3].toInt()
        val hour = bytes[index+4].toInt()
        val min = bytes[index+5].toInt()
        val second = bytes[index+6].toInt()
        c.set(year, month, day, hour, min, second)
        startTime = c.time
        index +=7
        size = toUInt(bytes.copyOfRange(index, index+4))
        index += 4

        recordTime = toUInt(bytes.copyOfRange(index, index+2))
        index+=2
        asleepTime = toUInt(bytes.copyOfRange(index, index+2))
        index +=2
        avgSpo2 = bytes[index].toInt()
        index++
        minSpo2 = bytes[index].toInt()
        index++
        drop3 = bytes[index].toInt()
        index++
        drop4 = bytes[index].toInt()
        index++
        druation90 = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        drops90 = bytes[index].toInt()
        index++
        percent90 = bytes[index].toFloat() / 100
        index++
        score = bytes[index].toFloat() / 10
        index++
        steps = toUInt(bytes.copyOfRange(index, index+4))
        index+=4
        index+=10 // reserved

        while(index < bytes.size) {
            spo2Wave.add(O2Sample(bytes.copyOfRange(index, index+5)))
            index+=5
        }

    }

    /**
     * every sample
     */
    inner class O2Sample {
        var spo2: Int  // -1:invalid value
        var pr: Int  // 65535: invalid value
        var acceleration: Int // Maximum 3-axis acceleration vector sum in 2 seconds

        @ExperimentalUnsignedTypes
        constructor(bytes: ByteArray) {
            var index = 0
            spo2 = bytes[index].toInt()
            index++
            pr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            acceleration = bytes[index].toUByte().toInt()
//            index++
            // reserve 1
        }
    }


    override fun toString(): String {
        return Gson().toJson(this)
    }
}