package com.lepu.lepuble.ble.cmd

import android.os.Parcelable
import com.blankj.utilcode.util.LogUtils
import com.lepu.lepuble.utils.ByteUtils
import com.lepu.lepuble.utils.toUInt
import kotlinx.android.parcel.Parcelize
import org.json.JSONObject

class OxyBleResponse{

    @ExperimentalUnsignedTypes
    class OxyResponse(bytes: ByteArray) {
        var no:Int
        var len: Int
        var state: Boolean
        var content: ByteArray

        init {
            state = bytes[1].toInt() == 0x00
            no = toUInt(bytes.copyOfRange(3, 5))
            len = bytes.size - 8
            content = bytes.copyOfRange(7, 7 + len)
        }
    }


    @ExperimentalUnsignedTypes
    @Parcelize
    class RtWave constructor(var bytes: ByteArray) : Parcelable {
        var content: ByteArray = bytes
        var spo2: Int
        var pr: Int
        var battery: Int
        var batteryState: String // 0 -> not charging; 1 -> charging; 2 -> charged
        var pi: Int
        var state: String //1-> lead on; 0-> lead off; other
        var len: Int
        var waveByte: ByteArray
        var wFs: IntArray? = null
        var wByte: ByteArray? = null

        init {
            spo2 = bytes[0].toUInt().toInt()
            pr = toUInt(bytes.copyOfRange(1, 3))
            battery = bytes[3].toUInt().toInt()
            batteryState = bytes[4].toUInt().toString()
            pi = bytes[5].toUInt().toInt()
            state = bytes[6].toUInt().toString()
            len = toUInt(bytes.copyOfRange(10, 12))
            waveByte = bytes.copyOfRange(12, 12 + len)
            wFs = IntArray(len)
            wByte = ByteArray(len)
            for (i in 0 until len) {
                var temp = ByteUtils.byte2UInt(waveByte[i])
                if (temp == 156) {
                    if (i==0) {
                        temp = ByteUtils.byte2UInt(waveByte[i+1])
                    } else if (i == len-1) {
                        temp = ByteUtils.byte2UInt(waveByte[i-1])
                    } else {
                        temp = (ByteUtils.byte2UInt(waveByte[i-1]) + ByteUtils.byte2UInt(waveByte[i+1]))/2
                    }
                }

                wFs!![i] = temp
                wByte!![i] = (100 - temp/2).toByte()
            }
        }
    }

    @Parcelize
    class OxyInfo (val bytes: ByteArray) : Parcelable {
        var region: String
        var model: String
        var hwVersion: String // hardware version
        var swVersion: String // software version
        var btlVersion: String
        var pedTar: Int
        var sn: String
        var curTime: String
//        var battery: Int
        var batteryState: String  // 0 -> not charging; 1 -> charging; 2 -> charged
        var oxiThr: Int
        var motor: String
        var mode: String
        var fileList: String

        init {
            val str = String(bytes)
            LogUtils.d("O2 Info: $str")
            val infoStr = JSONObject(str)
            region = infoStr.getString("Region")
            model = infoStr.getString("Model")
            hwVersion = infoStr.getString("HardwareVer")
            swVersion = infoStr.getString("SoftwareVer")
            btlVersion = infoStr.getString("BootloaderVer")
            pedTar = infoStr.getString("CurPedtar").toInt()
            sn = infoStr.getString("SN")
            curTime = infoStr.getString("CurTIME")
            //            battery = infoStr.getString("CurBAT").toIntOrNull() // 100%, 难解，不管
            batteryState = infoStr.getString("CurBatState")
            oxiThr = infoStr.getString("CurOxiThr").toInt()
            motor = infoStr.getString("CurMotor")
            mode = infoStr.getString("CurMode")
            fileList = infoStr.getString("FileList")
        }

    }

    @Parcelize
    class OxyFile(val name: String, val size: Int) : Parcelable  {
        var fileName: String
        var fileSize: Int
        var fileContent: ByteArray
        var index: Int  // 标识当前下载index

        init {
            fileName = name
            fileSize = size
            fileContent = ByteArray(size)
            index = 0
        }

        fun addContent(bytes: ByteArray) {
            if (index >= fileSize) {
                return  // 已下载完成
            } else {
                System.arraycopy(bytes, 0, fileContent, index, bytes.size)
                index += bytes.size
            }
        }
    }
}