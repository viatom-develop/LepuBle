package com.lepu.lepuble.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MonitorViewModel: ViewModel() {
    val deviceName: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val connect: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val hr: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val pr: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val spo2: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val pi: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val battery: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
}