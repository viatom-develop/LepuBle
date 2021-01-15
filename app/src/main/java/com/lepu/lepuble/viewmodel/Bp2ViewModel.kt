package com.lepu.lepuble.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lepu.lepuble.ble.obj.LepuDevice

class Bp2ViewModel: ViewModel() {

    val connect: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val bp2: MutableLiveData<LepuDevice> by lazy {
        MutableLiveData<LepuDevice>()
    }

    val battery: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val status: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val duration: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val hr: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val sys: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val dia: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val mean: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val pr: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    // draw ecg
    val dataSrc: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
}