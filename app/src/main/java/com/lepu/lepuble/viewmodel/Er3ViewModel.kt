package com.lepu.lepuble.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lepu.lepuble.ble.obj.LepuDevice

class Er3ViewModel : ViewModel() {

    val connect: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val er3: MutableLiveData<LepuDevice> by lazy {
        MutableLiveData<LepuDevice>()
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

    // measure duration
    val duration: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    // draw
    val dataSrc: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
}