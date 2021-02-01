package com.lepu.lepuble.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.LogUtils
import com.lepu.lepuble.BuildConfig
import com.lepu.lepuble.R
import com.lepu.lepuble.fragments.*
import com.lepu.lepuble.objs.Bluetooth
import com.lepu.lepuble.vals.curModel
import com.lepu.lepuble.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val mainModel : MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUI()
        observeLiveEventObserver()
        observeLiveDataObserve()
    }

    private fun initUI() {
        toolbar_title.text = BuildConfig.FLAVOR.toUpperCase(Locale.ROOT)
        connect.setOnClickListener {
            val i = Intent(this, SearchActivity::class.java)
            startActivity(i)
        }

        initFragments()
    }

    private fun initFragments() {
        val fragment: Fragment
        LogUtils.d(BuildConfig.FLAVOR, "current model: $curModel")
        when(curModel) {
            Bluetooth.MODEL_ER1  -> {
                fragment = Er1Fragment.newInstance()
            }
            Bluetooth.MODEL_ER2 -> {
                fragment = Er1Fragment.newInstance()
            }
            Bluetooth.MODEL_ER3 -> {
                fragment = Er3Fragment.newInstance()
            }
            Bluetooth.MODEL_CHECKO2 -> {
                fragment = OxyFragment.newInstance()
            }
//            Bluetooth.MODEL_AIRBP -> {}
            Bluetooth.MODEL_S1 -> {
                fragment = S1Fragment.newInstance()
            }
            Bluetooth.MODEL_BP2 -> {
                fragment = Bp2Fragment.newInstance()
            }
            Bluetooth.MODEL_KCA -> {
                fragment = KcaFragment.newInstance()
            }
            Bluetooth.MODEL_P1 -> {
                fragment = P1Fragment.newInstance()
            }

            else -> {
                fragment = Er1Fragment.newInstance()
            }
        }

        val trans = supportFragmentManager.beginTransaction()
        trans.add(R.id.container, fragment)
        trans.commitAllowingStateLoss()
    }

    private fun observeLiveEventObserver() {

    }

    private fun observeLiveDataObserve() {

    }
}