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

    val frequency: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val bandwidth: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val raise: MutableLiveData<Float> by lazy {
        MutableLiveData<Float>()
    }
    val fall: MutableLiveData<Float> by lazy {
        MutableLiveData<Float>()
    }
    val duration: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val rest: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val channelA: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val channelB: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
}