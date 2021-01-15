package com.lepu.lepuble.ble.cmd

import com.lepu.lepuble.utils.toUInt

object Bp2Response {

    const val DataTypeBping : Int = 0x00 //bp measuring
    const val DataTypeBpResult : Int = 0x01 //bp result
    const val DataTypeEcging : Int = 0x02 //ecg measuring
    const val DataTypeEcgResult : Int = 0x03 //ecg result

    const val STATUS_SLEEP = 0						    	//sleep
    const val STATUS_MEMERY = 1				                //memory
    const val STATUS_CHARGE = 2								//charging
    const val STATUS_READY = 3								//ready
    const val STATUS_BP_MEASURING = 4						//bp measuring
    const val STATUS_BP_MEASURE_END = 5						//bp result
    const val STATUS_ECG_MEASURING = 6						//ecg measuring
    const val STATUS_ECG_MEASURE_END = 7					//ecg result

    /**
     * Rtdata
     */
    @ExperimentalUnsignedTypes
    class RtData(var bytes: ByteArray) {
        val param: RtParam
        val wave: RtWave

        init {
            param = RtParam(bytes.copyOfRange(0, 9))
            wave = RtWave(bytes.copyOfRange(9, bytes.size))
        }

        override fun toString(): String {
            return """
                $param
                $wave
            """.trimIndent()
        }
    }

    /**
     * RtParam
     */
    @ExperimentalUnsignedTypes
    class RtParam(var bytes: ByteArray) {
        var status: Int
        var batteryState: Int
        var batteryLevel: Int
        var batteryVol: Int
        var contentLen = 9

        init {
            var index = 0
            status = bytes[index].toInt()
            index++
            batteryState = bytes[index].toInt()
            index++
            batteryLevel = bytes[index].toInt()
            index++
            batteryVol = toUInt(bytes.copyOfRange(index, index+2))
            index+=2
            // reserve 4
        }

        override fun toString(): String {
            return """
                status: $status
                battery: $batteryLevel
            """.trimIndent()
        }
    }

    /**
     * Real-time wave
     *  type
     *  data
     *  waveform
     */
    @ExperimentalUnsignedTypes
    class RtWave(var bytes: ByteArray) {
        var type: Int
        var dataBping: DataBping? = null
        var dataBpResult: DataBpResult? = null
        var dataEcging: DataEcging? = null
        var dataEcgResult: DataEcgResult? = null
        var waveLen: Int
        var wave: ByteArray? = null

        init {
            var index = 0
            type = bytes[index].toInt()
            index++
            when(type) {
                DataTypeBping -> dataBping = DataBping(bytes.copyOfRange(index, index+20))
                DataTypeBpResult -> dataBpResult = DataBpResult(bytes.copyOfRange(index, index+20))
                DataTypeEcging -> dataEcging = DataEcging(bytes.copyOfRange(index, index+20))
                DataTypeEcgResult -> dataEcgResult = DataEcgResult(bytes.copyOfRange(index, index+20))
            }
            index+=20
            if (bytes.size > 21) {
                waveLen = toUInt(bytes.copyOfRange(index, index+2))
                index+=2
                wave = bytes.copyOfRange(index, index+(2*waveLen))
            } else {
                waveLen = 0
            }

        }

        override fun toString(): String {
            return """
                $type
                $dataBping
                $dataBpResult
                $dataEcging
                $dataEcgResult
                wave len: $waveLen
            """.trimIndent()
        }
    }

    /**
     * data type = 0
     */
    @ExperimentalUnsignedTypes
    class DataBping(var bytes: ByteArray) {
        var isDeflate: Boolean
        var pressure: Int
        var isPulseDetect: Boolean
        var pr: Int
        var contentLen: Int = 20

        init {
            var index = 0
            isDeflate = bytes[index] == 0x01.toByte()
            index++
            pressure = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            isPulseDetect = bytes[index] == 0x01.toByte()
            index++
            pr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            // reserve 14
        }

        override fun toString(): String {
            return """
                Bping => isDeflate: $isDeflate, pressure: $pressure
            """.trimIndent()
        }
    }

    /**
     * data type = 1
     */
    @ExperimentalUnsignedTypes
    class DataBpResult(var bytes: ByteArray) {
        var isDeflate: Boolean
        var pressure: Int
        var sys: Int
        var dia: Int
        var mean: Int
        var pr: Int
        var state: Byte
        var diagnostic: Byte
        var contentLen: Int = 20

        init {
            var index = 0
            isDeflate = bytes[index] == 0x01.toByte()
            index++
            pressure = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            sys = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            dia = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            mean = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            pr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            state = bytes[index]
            index++
            diagnostic = bytes[index]
            index++
            // reserve 7
        }

        override fun toString(): String {
            return """
                Bp Result => isDeflate:$isDeflate, $sys $dia $pr
            """.trimIndent()
        }
    }

    /**
     * data type = 2
     */
    @ExperimentalUnsignedTypes
    class DataEcging(var bytes: ByteArray) {
        var duration: Int
        var status: ByteArray
        var hr: Int
        var contentLen: Int = 20

        init {
            var index = 0
            duration = toUInt(bytes.copyOfRange(index, index+4))
            index+=4
            status = bytes.copyOfRange(index, index+4)
            index+=4
            hr = toUInt(bytes.copyOfRange(index, index+2))
            index+=2
            // reserve 10
        }

        override fun toString(): String {
            return """
                Ecging => duration: $duration hr:$hr
            """.trimIndent()
        }
    }

    /**
     * data type = 3
     */
    @ExperimentalUnsignedTypes
    class DataEcgResult(var bytes: ByteArray) {
        var result: ByteArray
        var hr: Int
        var qrs: Int
        var pvcs: Int
        var qtc: Int
        var contentLen: Int = 20

        init {
            var index = 0
            result = bytes.copyOfRange(index, index+4)
            index += 4
            hr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            qrs = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            pvcs = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            qtc = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            // reserve 8
        }

        override fun toString(): String {
            return """
                Ecg Result=> hr: $hr, qrs: $qrs, pvcs: $pvcs
            """.trimIndent()
        }
    }
}