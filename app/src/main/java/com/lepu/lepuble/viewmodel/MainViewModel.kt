package com.lepu.lepuble.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.lepuble.ble.BleLogs
import com.lepu.lepuble.objs.BleLogItem
import com.lepu.lepuble.objs.Bluetooth
import com.lepu.lepuble.vals.EventMsgConst
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {

    val er1DeviceName: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }
    val er1Bluetooth: MutableLiveData<Bluetooth> by lazy {
        MutableLiveData<Bluetooth>()
    }

    val oxyDeviceName: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }
    val oxyBluetooth: MutableLiveData<Bluetooth> by lazy {
        MutableLiveData<Bluetooth>()
    }

    val kcaDeviceName: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val kcaBluetooth: MutableLiveData<Bluetooth> by lazy {
        MutableLiveData<Bluetooth>()
    }

//    val relayId : MutableLiveData<String> by lazy {
//        MutableLiveData<String>()
//    }

    val socketState: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val hostIp : MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }

    val hostPort : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val wifiRssi : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

//    val totalSend = MutableLiveData<Int>().apply {
//        value = 0
//    }
//    val totalReceive = MutableLiveData<Int>().apply {
//        value = 0
//    }
//    val start : MutableLiveData<Long> by lazy {
//        MutableLiveData<Long>()
//    }
//    val logs = MutableLiveData<Array<BleLogItem>>().apply {
//        value = emptyArray<BleLogItem>()
//    }
//
//    fun add(item: BleLogItem) {
////        logs.value = logs.value?.plus(item)
////        if (item.type == BleLogItem.SEND) {
////            totalSend.value = totalSend.value?.plus(item.content.size)
////        }
////        if (item.type == BleLogItem.RECEIVE) {
////            totalReceive.value = totalReceive.value?.plus(item.content.size)
////        }
//        GlobalScope.launch {
//            if (item.type == BleLogItem.SEND) {
//                totalSend.postValue(totalSend.value?.plus(item.content.size))
//            }
//            if (item.type == BleLogItem.RECEIVE) {
//                totalReceive.postValue(totalReceive.value?.plus(item.content.size))
//            }
//            logs.postValue(logs.value?.plus(item))
//        }
//
//    }
//    fun clear() {
//        totalSend.postValue(0)
//        totalReceive.postValue(0)
//        start.postValue(0L)
//        logs.postValue(emptyArray<BleLogItem>())
//    }
}