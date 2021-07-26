package com.lepu.lepuble.objs

class CmdListItem {
    var type: String
    var content: String
    var time: Long

    constructor(type: String, content: String) {
        this.type = type
        this.content = content
        this.time = System.currentTimeMillis()
    }
}