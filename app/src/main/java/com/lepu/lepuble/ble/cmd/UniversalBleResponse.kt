package com.lepu.lepuble.ble.cmd

import com.lepu.lepuble.utils.toUInt

object UniversalBleResponse {

    /**
     * this is the object for Lepu Bluetooth protocol
     * 乐普设备蓝牙通信层的结构体
     */
    @ExperimentalUnsignedTypes
    class LepuResponse(var bytes: ByteArray) {
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

    /**
     * Response for {@link UniversalBleCmd#READ_FILE_LIST}
     */
    @ExperimentalUnsignedTypes
    class FileList(var bytes: ByteArray) {
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

    /**
     * download file
     * create a LepuFile when you start download a file
     * use #addContent when you receive content
     */
    class LepuFile(var name:String, val size: Int) {
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
}