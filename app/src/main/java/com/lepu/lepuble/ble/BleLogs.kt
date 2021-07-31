package com.lepu.lepuble.ble

import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.lepuble.objs.BleLogItem
import com.lepu.lepuble.vals.EventMsgConst
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object BleLogs {
    var totalSend = 0
    var totalReceive = 0
    var start: Long = 0
    var logs = mutableListOf<BleLogItem>()

    public fun add(item: BleLogItem) {
        GlobalScope.launch {
            logs.add(item)
            if (item.type == BleLogItem.SEND) {
                totalSend += item.content.size
            }
            if (item.type == BleLogItem.RECEIVE) {
                totalReceive += item.content.size
            }

            LiveEventBus.get(EventMsgConst.EventBleLog).post(true)
        }
    }

    public fun clear() {
        GlobalScope.launch {
            totalSend = 0
            totalReceive = 0
            start = System.currentTimeMillis()
            logs = mutableListOf<BleLogItem>()

            LiveEventBus.get(EventMsgConst.EventBleLog).post(true)
        }
    }
}