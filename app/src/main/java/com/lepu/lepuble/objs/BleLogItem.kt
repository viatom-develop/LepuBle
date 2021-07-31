package com.lepu.lepuble.objs

class BleLogItem {
    var type: String
    var content: ByteArray
    var time: Long

    constructor(type: String, content: ByteArray) {
        this.type = type
        this.content = content
        this.time = System.currentTimeMillis()
    }

    companion object {
        val SEND = "send"
        val RECEIVE = "recv"
    }
}