package com.lepu.lepuble.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.function.Predicate

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

    val ecgList: MutableLiveData<MutableList<Float>> by lazy {
        MutableLiveData<MutableList<Float>>()
    }

    val oxiList: MutableLiveData<MutableList<Float>> by lazy {
        MutableLiveData<MutableList<Float>>()
    }

    fun addEcg(floatArray: FloatArray) {
        val tmp = ecgList.value!!
        tmp.addAll(floatArray.toMutableList())
        for(i in 0..5) {
            tmp.removeAt(0)
        }
        ecgList.value = tmp
    }
    fun addOxi(floatArray: FloatArray) {
        val tmp = ecgList.value!!
        tmp.addAll(floatArray.toMutableList())
        for(i in 0..5) {
            tmp.removeAt(0)
        }
        oxiList.value = tmp
    }

    /**
     * the ecg & oxi view X-axis size
     */
    fun initList(size: Int) {
        ecgList.value = FloatArray(size).toMutableList()

        oxiList.value = FloatArray(size).toMutableList()
    }
}