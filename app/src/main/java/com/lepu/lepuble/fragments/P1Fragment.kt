package com.lepu.lepuble.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.lepuble.R
import com.lepu.lepuble.ble.P1BleInterface
import com.lepu.lepuble.objs.Bluetooth
import com.lepu.lepuble.vals.EventMsgConst
import com.lepu.lepuble.viewmodel.MainViewModel
import com.lepu.lepuble.viewmodel.P1ViewModel
import kotlinx.android.synthetic.main.fragment_p1.*

class P1Fragment : Fragment() {

    private lateinit var bleInterface: P1BleInterface

    private val model: P1ViewModel by viewModels()
    private val activityModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bleInterface = P1BleInterface()
        bleInterface.setViewModel(model)
        addLiveDataObserver()
        addLiveEventObserver()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_p1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        power.setOnClickListener {
            if (model.power.value == null) {
                model.power.value = false
            }
            model.power.value?.apply {
                model.power.value = !this
                bleInterface.turnOn(!this)
            }
        }

        mode_1.setOnClickListener { bleInterface.changeMode(0) }
        mode_2.setOnClickListener { bleInterface.changeMode(1) }
        mode_3.setOnClickListener { bleInterface.changeMode(2) }
        mode_4.setOnClickListener { bleInterface.changeMode(3) }
        mode_5.setOnClickListener { bleInterface.changeMode(4) }

        strength.addOnChangeListener { slider, value, fromUser ->
            bleInterface.changeStrength(value.toInt())
        }

        heat.setOnClickListener {
            if (model.heat.value == null)
                model.heat.value = false

            model.heat.value?.apply {
                model.heat.value = !this
                bleInterface.setHeat(!this)
            }
        }

        duration.addOnChangeListener { slider, value, fromUser ->
            bleInterface.setDuration((3-value/5).toInt())
        }
    }

    private fun addLiveEventObserver() {
        model.sn.observe(this, {
            sn.text = "sn: $it"
        })

        model.connect.observe(this, {
            if (it) {
                ble_state.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                ble_state.setImageResource(R.mipmap.bluetooth_error)
            }
        })
        model.battery.observe(this, {
            battery.text = "$it%"
        })
        model.heat.observe(this, {
            heat.isChecked = it
        })
        model.power.observe(this, {
            if (it) {
                power.text = "停止按摩"
            } else {
                power.text = "开始按摩"
            }
        })
        model.mode.observe(this, {
            clearMode()
            when(it) {
                0 -> mode_1.setTextColor(resources.getColor(R.color.colorRed))
                1 -> mode_2.setTextColor(resources.getColor(R.color.colorRed))
                2 -> mode_3.setTextColor(resources.getColor(R.color.colorRed))
                3 -> mode_4.setTextColor(resources.getColor(R.color.colorRed))
                4 -> mode_5.setTextColor(resources.getColor(R.color.colorRed))
            }
        })
        model.strength.observe(this, {
            strength.value = it.toFloat()
        })
        model.duration.observe(this, {
            duration.value = (3-it)*5.0f
            duration_val.text = "${(3-it)*5}分钟"
        })
    }

    private fun clearMode() {
        mode_1.setTextColor(resources.getColor(R.color.color_white))
        mode_2.setTextColor(resources.getColor(R.color.color_white))
        mode_3.setTextColor(resources.getColor(R.color.color_white))
        mode_4.setTextColor(resources.getColor(R.color.color_white))
        mode_5.setTextColor(resources.getColor(R.color.color_white))
    }

    private fun addLiveDataObserver() {
        LiveEventBus.get(EventMsgConst.EventDeviceChoosen)
                .observe(this, {
                    connect(it as Bluetooth)
                })
    }


    private fun connect(b: Bluetooth) {
        this@P1Fragment.context?.let { bleInterface.connect(it, b.device) }
    }

    companion object {
        @JvmStatic
        fun newInstance() = P1Fragment()
    }
}