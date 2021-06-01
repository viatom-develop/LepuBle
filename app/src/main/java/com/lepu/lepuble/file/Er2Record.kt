package com.lepu.lepuble.file

import com.blankj.utilcode.util.LogUtils
import com.lepu.lepuble.ble.obj.Er1DataController
import com.lepu.lepuble.utils.toUInt
import java.util.*

class Er2Record {

    private var data: ByteArray? = null
    private var fileVersion: String? = null
    private var recordingTime = 0
    private var waveData: ByteArray? = null
    private var waveFloats = mutableListOf<Float>()
    // 用于向服务器上传的 int
    private var waveInts= mutableListOf<Int>()
    private var dataCrc = 0
    private var magic = 0
    private var startTime: Long = 0

    constructor(bytes: ByteArray) {
        val len = bytes.size
        if (len < 30) {
            LogUtils.d("file error")
            return
        }

        this.data = bytes

        recordingTime = toUInt(bytes.copyOfRange(len-20, len-16))
        LogUtils.d("duration: $recordingTime")
        waveData = bytes.copyOfRange(10, recordingTime*125+10)

        val convert = DataConvert()
        for (i in waveData!!.indices) {
            val tmp = convert.unCompressAlgECG(waveData!![i])
            if (tmp.toInt() != -32768) {
                val mv = (tmp * (1.0035 * 1800) / (4096 * 178.74)).toFloat()
                waveFloats.add(mv)
                waveInts.add((mv*405.35).toInt())
            }
        }

        fileVersion = "V${bytes[0]}"
    }

    override fun toString(): String {
        return """
            fileVersion: $fileVersion
            duration: $recordingTime
            len: ${waveData?.size}
            data: $waveInts
        """.trimIndent()
    }
}