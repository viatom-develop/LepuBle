package com.lepu.lepuble.utils

val HEX_ARRAY = "0123456789ABCDEF".toCharArray()

fun ByteArray.toHex() = joinToString("") {
    String.format("%02X", (it.toInt() and 0xff))
}

fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }


fun add(ori: ByteArray?, add: ByteArray): ByteArray {
    if (ori == null) {
        return add
    }

    val new: ByteArray = ByteArray(ori.size + add.size)
    for ((index, value) in ori.withIndex()) {
        new[index] = value
    }

    for ((index, value) in add.withIndex()) {
        new[index + ori.size] = value
    }

    return new
}

@ExperimentalUnsignedTypes fun toUInt(bytes: ByteArray): Int {
    var result : UInt = 0u
    for (i in bytes.indices) {
        result = result or ((bytes[i].toUInt() and 0xFFu) shl 8*i)
    }

    return result.toInt()
}

fun toInt(bytes: ByteArray): Int {
    var result : Int = 0
    for (i in bytes.indices) {
        result = result or ((bytes[i].toInt() and 0xFF) shl 8*i)
    }

    return result
}

fun toUInt(byte: Byte): Int {
    return (byte.toUInt() and 0xFFu).toInt()
}

fun toString(bytes: ByteArray): String {
    var str = ""
    for (byte in bytes) {
        str += byte.toChar()
    }

    return str
}

fun getHexUppercase(b: Byte): String {
    val sb = StringBuilder()
    val lh: Int = (b.toInt() and 0x0f)
    val fh: Int = (b.toInt() and 0xf0) shr 4
    sb.append("0x")
    sb.append(HEX_ARRAY.get(fh))
    sb.append(HEX_ARRAY.get(lh))
    return sb.toString()
}

fun bytesToHex(bytes: ByteArray): String {
    val hexChars = CharArray(bytes.size * 2)
    for (j in bytes.indices) {
        val v: Int = bytes[j].toInt() and 0xFF
        hexChars[j * 2] = HEX_ARRAY.get(v ushr 4)
        hexChars[j * 2 + 1] = HEX_ARRAY.get(v and 0x0F)
    }
    return String(hexChars)
}