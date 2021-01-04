package com.lepu.lepuble

import android.app.Application
import com.jeremyliao.liveeventbus.LiveEventBus

class MyBleApp : Application() {


    override fun onCreate() {
        super.onCreate()

        // https://github.com/JeremyLiao/LiveEventBus/blob/master/docs/config.md
        LiveEventBus.config()
                .lifecycleObserverAlwaysActive(true)
                .enableLogger(false)

    }


}