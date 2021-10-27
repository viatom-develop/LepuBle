package com.lepu.lepuble.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lepu.lepuble.ble.cmd.Am300Obj
import com.lepu.lepuble.ble.obj.LepuDevice

class Am300bViewModel: ViewModel() {

    val connect: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

//    val 300b: MutableLiveData<LepuDevice> by lazy {
//        MutableLiveData<LepuDevice>()
//    }

    val sn: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val version: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val battery: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val emgState: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val emgPkg: MutableLiveData<Am300Obj.EmgPkg> by lazy {
        MutableLiveData<Am300Obj.EmgPkg>()
    }

    val emgLead: MutableLiveData<Am300Obj.EmgLeadOff> by lazy {
        MutableLiveData<Am300Obj.EmgLeadOff>()
    }

    val channel: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val is_ab: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    /**
     * A
     *
     */
    val frequency_a: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val bandwidth_a: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val raise_a: MutableLiveData<Float> by lazy {
        MutableLiveData<Float>()
    }
    val fall_a: MutableLiveData<Float> by lazy {
        MutableLiveData<Float>()
    }
    val duration_a: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val rest_a: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    /**
     * B
     *
     */
    val frequency_b: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val bandwidth_b: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val raise_b: MutableLiveData<Float> by lazy {
        MutableLiveData<Float>()
    }
    val fall_b: MutableLiveData<Float> by lazy {
        MutableLiveData<Float>()
    }
    val duration_b: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val rest_b: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }


    val channelA: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val channelB: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
}