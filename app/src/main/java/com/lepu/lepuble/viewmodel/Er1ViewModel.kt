package com.lepu.lepuble.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lepu.lepuble.ble.obj.Er1Device

class Er1ViewModel : ViewModel() {

    val connect: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val er1: MutableLiveData<Er1Device> by lazy {
        MutableLiveData<Er1Device>()
    }

    val battery: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val lead: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val hr: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    // 已测量时长
    val duration: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    // draw
    val dataSrc: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
}