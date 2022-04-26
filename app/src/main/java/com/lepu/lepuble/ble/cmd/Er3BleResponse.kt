package com.lepu.lepuble.ble.cmd

import com.blankj.utilcode.util.LogUtils
import com.lepu.lepuble.ble.obj.Er1DataController
import com.lepu.lepuble.file.uncompress.Er3Decompress
import com.lepu.lepuble.utils.toHex
import com.lepu.lepuble.utils.toUInt
import java.util.*

object Er3BleResponse {
    /**
     * 第一版的结构体
     * 只有心电数据
     */
    @ExperimentalUnsignedTypes
    class Er3RtData constructor(var bytes: ByteArray) {
        var content: ByteArray = bytes
        var param: Er1BleResponse.RtParam
        var wave: Er3RtWave

        init {
            param = Er1BleResponse.RtParam(bytes.copyOfRange(0, 20))
            wave = Er3RtWave(bytes.copyOfRange(20, bytes.size))
        }
    }

    @ExperimentalUnsignedTypes
    class Er3RtWave constructor(var bytes: ByteArray) {
        var content: ByteArray = bytes
        var len: Int
        val channels = 8
        var wave : ByteArray? = null
        var waveFs : FloatArray? = null

        init {
            len = toUInt(bytes.copyOfRange(0, 2))
            if (len > 0) {
                wave = bytes.copyOfRange(2, bytes.size)

                waveFs = FloatArray(len * channels)

                for (i in 0 until (len * channels)) {
                    waveFs!![i] = Er1DataController.byteTomV(wave!![2 * i], wave!![2 * i + 1])
                }
//                LogUtils.d(wave!!.toHex())
//                LogUtils.d(Arrays.toString(waveFs))
            }
        }
    }

    /**
     * 第二版的结构体
     * 包含心电数据、体温
     */
    class RtData constructor(var bytes: ByteArray) {
        // 48 bytes
        var runStatus: RunStatus
        var rtWave: RtWave

        init {
            runStatus = RunStatus(bytes.copyOfRange(0, 48))
            rtWave = RtWave(runStatus.lead, bytes.copyOfRange(48, bytes.size))
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    class RunStatus constructor(var bytes: ByteArray) {

        var hr: Int
        // 体温 无效值0xFFFF  e.g. 2500   temp = 25.0℃
        var temp: Float
        var spo2: Int
        // 0- 200  e.g. 25 : PI = 2.5
        var pi: Float
        var pr: Int
        var respRate: Int
        var flag: SysFlag
        var battery: Int
        var recordTime: Int
        var startTime: Date?
        var lead: LeadType
        // 一次性导联的sn
        var leadSn: String
        // 导联状态 bit0-11  I II III aVR aVL aVF V1 V2 V3 V4 V5 V6 	(0:ON  1:OFF)
        var leadState: List<Boolean> = List(8) { false }
        // reserved 6 byte

        init {
            LogUtils.d("RunStatus: ${bytes.toHex()}")
            var index = 0
            hr = toUInt(bytes.copyOfRange(index, index + 2))
            index += 2
            temp = toUInt(bytes.copyOfRange(index, index + 2)) / 100.toFloat()
            index += 2
            spo2 = toUInt(bytes[index])
            index++
            pi = toUInt(bytes[index]) / 10.toFloat()
            index++
            pr = toUInt(bytes.copyOfRange(index, index + 2))
            index += 2
            respRate = toUInt(bytes.copyOfRange(index, index + 2))
            index += 2
            flag = SysFlag(bytes.copyOfRange(index, index + 2))
            index += 2
            battery = toUInt(bytes[index])
            index++
            recordTime = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            startTime = null//
            index += 7
            lead = checkLead(bytes[index])
            index++
            leadSn = String(bytes.copyOfRange(index, index+15))
            index += 15
            index += 2 // state
            index += 6
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    class RtWave constructor(val lead: LeadType, var bytes: ByteArray) {

        var firstIndex : Int // 数据的第一个点
        var len: Int // 采样点数
        val channels = 8 // 简单处理起见，补全8 lead
        var wave : ByteArray? = null
        var waveMvs : FloatArray? = null

        init {

            var index = 0
            firstIndex = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            len = toUInt(bytes.copyOfRange(index, index+2))
            index += 2

            LogUtils.d("RtWave: ${bytes.toHex()}", "LeadType: $lead", len)

            if (len > 0) {
                val oriWave = bytes.copyOfRange(index, bytes.size)
                // decompress
                val num = if (lead == LeadType.TYPE_LEAD_12) {
                    8
                } else {
                    4
                }
                val decompress = Er3Decompress(num)
                val decompressData = mutableListOf<Float>()
                for (b in oriWave) {
                    val tmp = decompress.Decompress(b)
                    if (tmp != null) {
                        for (i in tmp) {
                            decompressData.add((i * (1.0035 * 1800) / (4096 * 178.74)).toFloat())
                        }
                    }
                }

                val oriMvs = decompressData.toFloatArray()
                LogUtils.d("after decompress, size: ${oriMvs.size}")

//                waveMvs = FloatArray(len * channels)
                waveMvs = FloatArray(0)
                val tmpFs = mutableListOf<Float>()
                val nullMvArr = FloatArray(len) {0f}

                when(lead) {
                    LeadType.TYPE_LEAD_12 -> {
                        val lead_size = 8
                        waveMvs = oriMvs

                    }
                    LeadType.TYPE_LEAD_6 -> {
                        val lead_size = 4
                        for (i in oriMvs.indices step lead_size) {
                            tmpFs.add(0f)
                            tmpFs.add(oriMvs[i+1])
                            tmpFs.add(oriMvs[i+2])
                            tmpFs.add(oriMvs[i+3])
                            tmpFs.add(0f)
                            tmpFs.add(0f)
                            tmpFs.add(0f)
                            tmpFs.add(oriMvs[i])
                        }
                        waveMvs = tmpFs.toFloatArray()
                    }
                    LeadType.TYPE_LEAD_5 -> {
                        val lead_size = 4
                        for (i in oriMvs.indices step lead_size) {
                            tmpFs.add(0f)
                            tmpFs.add(oriMvs[i+1])
                            tmpFs.add(oriMvs[i+2])
                            tmpFs.add(oriMvs[i+3])
                            tmpFs.add(0f)
                            tmpFs.add(0f)
                            tmpFs.add(0f)
                            tmpFs.add(0f)
                        }
                        waveMvs = tmpFs.toFloatArray()

                    }
                    LeadType.TYPE_LEAD_3 -> {
                        val lead_size = 4
                        for (i in oriMvs.indices step lead_size) {
                            tmpFs.add(0f)
                            tmpFs.add(0f)
                            tmpFs.add(oriMvs[i+2])
                            tmpFs.add(0f)
                            tmpFs.add(0f)
                            tmpFs.add(0f)
                            tmpFs.add(0f)
                        }
                        waveMvs = tmpFs.toFloatArray()

                    }
                    LeadType.TYPE_LEAD_3_TEMP -> {
                        val lead_size = 4
                        for (i in oriMvs.indices step lead_size) {
                            tmpFs.add(0f)
                            tmpFs.add(oriMvs[i+1])
                            tmpFs.add(0f)
                            tmpFs.add(0f)
                            tmpFs.add(0f)
                            tmpFs.add(0f)
                            tmpFs.add(0f)
                        }
                        waveMvs = tmpFs.toFloatArray()

                    }
                    LeadType.TYPE_LEAD_3_LEG -> {
                        val lead_size = 4
                        for (i in oriMvs.indices step lead_size) {
                            tmpFs.add(0f)
                            tmpFs.add(oriMvs[i+1])
                            tmpFs.add(oriMvs[i+2])
                            tmpFs.add(0f)
                            tmpFs.add(0f)
                            tmpFs.add(0f)
                            tmpFs.add(0f)
                            tmpFs.add(0f)
                        }
                        waveMvs = tmpFs.toFloatArray()

                    }
                    LeadType.TYPE_LEAD_5_LEG -> {
                        val lead_size = 4
                        for (i in oriMvs.indices step lead_size) {
                            tmpFs.add(0f)
                            tmpFs.add(oriMvs[i+1])
                            tmpFs.add(oriMvs[i+2])
                            tmpFs.add(0f)
                            tmpFs.add(0f)
                            tmpFs.add(0f)
                            tmpFs.add(0f)
                            tmpFs.add(oriMvs[i])
                        }
                        waveMvs = tmpFs.toFloatArray()

                    }
                }
//                LogUtils.d(wave!!.toHex())
//                LogUtils.d(Arrays.toString(waveFs))
            }

        }
    }

    class SysFlag constructor(var bytes: ByteArray) {

    }

    // 导联类型
    enum class LeadType {
        TYPE_LEAD_12,   //12导   V6 I II V1 V2 V3 V4 V5
        TYPE_LEAD_6,    //6导    V5  I  II  V1
        TYPE_LEAD_5,    //5导    X  I  II  V1
        TYPE_LEAD_3,    //3导    X  X  II  X
        TYPE_LEAD_3_TEMP,   //3导带体温 X  I  X  X
        TYPE_LEAD_3_LEG,    //3导胸贴  X  I  II  X
        TYPE_LEAD_5_LEG,    //5导胸贴  V5  I  II  X


    }

    fun checkLead(byte: Byte): LeadType = when(byte) {
        0x00.toByte() -> LeadType.TYPE_LEAD_12
        0x01.toByte() -> LeadType.TYPE_LEAD_6
        0x02.toByte() -> LeadType.TYPE_LEAD_5
        0x03.toByte() -> LeadType.TYPE_LEAD_3
        0x04.toByte() -> LeadType.TYPE_LEAD_3_TEMP
        0x05.toByte() -> LeadType.TYPE_LEAD_3_LEG
        0x06.toByte() -> LeadType.TYPE_LEAD_5_LEG
        else -> {LeadType.TYPE_LEAD_12}
    }


}

private fun addFloats(fs1: MutableList<Float>, fs2: FloatArray) {
    for (f in fs2) {
        fs1.add(f)
    }
}
