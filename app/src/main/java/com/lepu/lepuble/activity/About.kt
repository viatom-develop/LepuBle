package com.lepu.lepuble.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lepu.lepuble.R
import com.lepu.lepuble.ble.obj.LepuDevice
import kotlinx.android.synthetic.main.activity_about.*

class About : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        initUi()
    }

    @ExperimentalUnsignedTypes
    private fun initUi() {
        val i = intent
        val device = i.getParcelableExtra<LepuDevice>("er3")

        if (device != null) {
            sn.text = device.sn
            version.text = device.fwV
        }
    }
}