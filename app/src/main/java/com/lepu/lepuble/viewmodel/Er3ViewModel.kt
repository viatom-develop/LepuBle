package com.lepu.lepuble.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lepu.lepuble.ble.obj.LepuDevice

class Er3ViewModel : ViewModel() {

    val connect: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    @OptIn(ExperimentalUnsignedTypes::class)
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

    val spo2: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val temp: MutableLiveData<Float> by lazy {
        MutableLiveData<Float>()
    }

    // measure duration
    val duration: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    // draw
    val dataSrc1: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val dataSrc2: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val dataSrc3: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val dataSrc4: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val dataSrc5: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val dataSrc6: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val dataSrc7: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val dataSrc8: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val dataSrc9: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val dataSrc10: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val dataSrc11: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val dataSrc12: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
}