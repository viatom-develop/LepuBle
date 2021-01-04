package com.lepu.lepuble.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lepu.lepuble.ble.obj.KcaBpConfig

class KcaConfigModel : ViewModel()  {

    val config: MutableLiveData<KcaBpConfig.MeasureConfig> by lazy {
        MutableLiveData<KcaBpConfig.MeasureConfig>()
    }
}