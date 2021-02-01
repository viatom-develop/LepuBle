package com.lepu.lepuble.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class P1ViewModel: ViewModel() {

    val connect: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val mode: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val battery: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val duration: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val strength: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val power: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val heat: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

}