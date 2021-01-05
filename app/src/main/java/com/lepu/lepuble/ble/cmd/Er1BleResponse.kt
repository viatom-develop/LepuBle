package com.lepu.lepuble.ble.cmd

import android.os.Parcelable
import com.lepu.lepuble.ble.obj.Er1DataController
import com.lepu.lepuble.utils.toUInt
import kotlinx.android.parcel.Parcelize

object Er1BleResponse {

    @ExperimentalUnsignedTypes
    @Parcelize
    class Er1Response @ExperimentalUnsignedTypes constructor(var bytes: ByteArray) : Parcelable {
        var cmd: Int
        var pkgType: Byte
        var pkgNo: Int
        var len: Int
        var content: ByteArray

        init {
            cmd = (bytes[1].toUInt() and 0xFFu).toInt()
            pkgType = bytes[3]
            pkgNo = (bytes[4].toUInt() and 0xFFu).toInt()
            len = toUInt(bytes.copyOfRange(5, 7))
            content = bytes.copyOfRange(7, 7+len)
        }
    }


    @Parcelize
    class RtData @ExperimentalUnsignedTypes constructor(var bytes: ByteArray) : Parcelable {
        var content: ByteArray = bytes
        var param: RtParam
        var wave: RtWave

        init {
            param = RtParam(bytes.copyOfRange(0, 20))
            wave = RtWave(bytes.copyOfRange(20, bytes.size))
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class RtParam @ExperimentalUnsignedTypes constructor(var bytes: ByteArray) : Parcelable {
        var hr: Int
        var sysFlag: Byte
        var battery: Int
        var recordTime: Int = 0
        var runStatus: Byte
        var leadOn: Boolean
        // reserve 11

        init {
            hr = toUInt(bytes.copyOfRange(0, 2))
            sysFlag = bytes[2]
            battery = (bytes[3].toUInt() and 0xFFu).toInt()
            if (bytes[8].toUInt() and 0x02u == 0x02u) {
                recordTime = toUInt(bytes.copyOfRange(4, 8))
            }
            runStatus = bytes[8]
            leadOn = (bytes[8].toUInt() and 0x07u) != 0x07u
        }
    }

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
        }
    }

    class Er3RtData constructor(var bytes: ByteArray) {
        var content: ByteArray = bytes
        var param: RtParam
        var wave: Er3RtWave

        init {
            param = RtParam(bytes.copyOfRange(0, 20))
            wave = Er3RtWave(bytes.copyOfRange(20, bytes.size))
        }
    }

    @ExperimentalUnsignedTypes
    class Er3RtWave constructor(var bytes: ByteArray) {
        var content: ByteArray = bytes
        var len: Int
        val lead = 4
        var wave = mutableListOf<ByteArray>()
        var wFs : FloatArray? = null

        init {
            len = toUInt(bytes.copyOfRange(0, 2))
            val lead1 = mutableListOf<Byte>()
            val lead2 = mutableListOf<Byte>()
            val lead3 = mutableListOf<Byte>()
            val lead4 = mutableListOf<Byte>()
            for (i in 2 until bytes.size step 8) {
                lead1.add(bytes[i])
                lead1.add(bytes[i+1])
                lead2.add(bytes[i+2])
                lead2.add(bytes[i+3])
                lead3.add(bytes[i+4])
                lead3.add(bytes[i+5])
                lead4.add(bytes[i+6])
                lead4.add(bytes[i+7])
            }
            wave.add(lead1.toByteArray())
            wave.add(lead2.toByteArray())
            wave.add(lead3.toByteArray())
            wave.add(lead4.toByteArray())

            wFs = FloatArray(len)
            for (i in 0 until len) {
                wFs!![i] = Er1DataController.byteTomV(lead1[2 * i], lead1[2 * i + 1])
            }
        }
    }


    class Er1File(val name:String, val size: Int) {
        var fileName: String
        var fileSize: Int
        var content: ByteArray
        var index: Int // 标识当前下载index

        init {
            fileName = name
            fileSize = size
            content = ByteArray(size)
            index = 0
        }

        fun addContent(bytes: ByteArray) {
            if (index >= fileSize) {
                return // 已下载完成
            } else {
                System.arraycopy(bytes, 0, content, index, bytes.size)
                index += bytes.size
            }
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class Er1FileList constructor(var bytes: ByteArray) : Parcelable {
        var size: Int
        var fileList = mutableListOf<ByteArray>()

        init {
            size=bytes[0].toUInt().toInt()
            for (i in  0 until size) {
                fileList.add(bytes.copyOfRange(1+i*16, 17+i*16))
            }
        }

        override fun toString(): String {
            var str = ""
            for (bs in fileList) {
                str += String(bs)
                str += ","
            }
            return str
        }
    }
}