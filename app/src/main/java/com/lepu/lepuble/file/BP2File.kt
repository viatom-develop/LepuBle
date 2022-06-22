package com.lepu.lepuble.file

import com.blankj.utilcode.util.LogUtils
import com.lepu.lepuble.utils.toUInt

class BP2File {
    companion object {
        const val FILE_TYPE_BP = 1
        const val FILE_TYPE_ECG = 2
    }

    private var data: ByteArray? = null
    private var fileVersion: String? = null
    private var fileType: Int = 0   // 1: blood press; 2: ecg
    private var recordingTime = 0  // record time, unix timestamp
    private var bpStruct: BpStruct? = null
    private var ecgStruct: EcgStruct? = null



    @OptIn(ExperimentalUnsignedTypes::class)
    constructor(bytes: ByteArray) {
        val len = bytes.size

        this.data = bytes
        fileVersion = "V${bytes[0]}"
        fileType = toUInt(bytes[1])
        recordingTime = toUInt(bytes.copyOfRange(2, 6))
        // reserved 4
        when(fileType) {
            FILE_TYPE_BP -> bpStruct = BpStruct(bytes.copyOfRange(10, bytes.size))
            FILE_TYPE_ECG -> ecgStruct = EcgStruct(bytes.copyOfRange(10, bytes.size))
        }


        LogUtils.d("duration: $recordingTime")

    }

    @OptIn(ExperimentalUnsignedTypes::class)
    class BpStruct(bytes: ByteArray) {
//        private var statusCode: Int = 0
        public var sys: Int = 0
        public var dia: Int = 0
        public var mean: Int = 0
        public var pr: Int = 0
//        private var diagnosis: Int = 0

        init {
            var index = 0
            index++;  //statusCode
            sys = toUInt(bytes.copyOfRange(index,index+2))
            index+=2
            dia = toUInt(bytes.copyOfRange(index,index+2))
            index+=2
            mean = toUInt(bytes.copyOfRange(index,index+2))
            index+=2
            pr = toUInt(bytes[index])
            index++
            // reserved 20
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    class EcgStruct(bytes: ByteArray) {
        public var duration: Int = 0
        public var hr: Int = 0
        public var qrs: Int = 0
        public var pvcs: Int = 0
        public var qtc: Int = 0
        public var isCable: Boolean = false
        public var waveData: ByteArray? = null
        public var waveFloats = mutableListOf<Float>()  // ECG mV values
        // 用于向服务器上传的 int
        public var waveInts= mutableListOf<Int>() // for .txt file

        init {
            var index = 0
            duration = toUInt(bytes.copyOfRange(index, index+4))
            index+=4
            index+=2 // reserved 2 bytes
            index+=4 // diagnosis
            hr = toUInt(bytes.copyOfRange(index,index+2))
            index+=2
            qrs = toUInt(bytes.copyOfRange(index,index+2))
            index+=2
            pvcs = toUInt(bytes.copyOfRange(index,index+2))
            index+=2
            qtc = toUInt(bytes.copyOfRange(index,index+2))
            index+=2
            isCable = bytes[index].toInt() == (0x01)
            index++
            index+=19 // reserved 19

            // waveform
            waveData = bytes.copyOfRange(index, bytes.size)
            for (i in index until bytes.size step 2) {
                toUInt(bytes.copyOfRange(i, i+2)).apply {
                    waveInts.add(this)
                    waveFloats.add((this /322.79).toFloat())
                }

            }
        }
    }

    override fun toString(): String {
        return """
            fileVersion: $fileVersion
            timestamp: $recordingTime
            ecg: ${ecgStruct?.hr} / ${ecgStruct?.waveInts?.size}
            bp: ${bpStruct?.sys} / ${bpStruct?.dia}
        """.trimIndent()
    }

    /**
     * 输出保存为AI分析的txt文件内容
     * 返回字符串，然后保存在 txt文件中
     * version解释：
     *      1: 不带滤波配置
     *      2： 带滤波配置
     *  update at 2021/12/31: 算法已经支持滤波配置，请使用默认version =2
     */
    public fun toAIFile(version: Int = 2) : String? {
        if (fileType != FILE_TYPE_ECG) {
            return null
        }
        var file = ""
        if (version == 2) {
            file += "F-0-01,"
        }
        file += "125,II,322.79,"
        for (i in ecgStruct?.waveInts!!) {
            file += "$i,"
        }

        file = file.substring(0, file.length-1)

        return file
    }
}