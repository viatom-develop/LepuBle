package com.lepu.lepuble.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lepu.lepuble.ble.cmd.OxyBleResponse

class OxyViewModel : ViewModel() {

    val deviceName: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val connect: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val battery: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val info: MutableLiveData<OxyBleResponse.OxyInfo> by lazy {
        MutableLiveData<OxyBleResponse.OxyInfo>()
    }

    val lead: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val pr: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val spo2: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

//    val pi: MutableLiveData<Float> by lazy {
//        MutableLiveData<Float>()
//    }

    // draw
    val dataSrc: MutableLiveData<IntArray> by lazy {
        MutableLiveData<IntArray>()
    }

}