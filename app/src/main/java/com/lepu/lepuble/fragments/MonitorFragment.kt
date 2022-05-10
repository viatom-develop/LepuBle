package com.lepu.lepuble.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.lepuble.R
import com.lepu.lepuble.ble.MonitorBleInterface
import com.lepu.lepuble.ble.cmd.MonitorRtData
import com.lepu.lepuble.objs.Bluetooth
import com.lepu.lepuble.vals.EventMsgConst
import com.lepu.lepuble.viewmodel.MainViewModel
import com.lepu.lepuble.viewmodel.MonitorViewModel
import kotlinx.android.synthetic.main.fragment_monitor.*
import kotlinx.android.synthetic.main.fragment_monitor.device_sn
import kotlinx.android.synthetic.main.fragment_monitor.battery
import kotlinx.android.synthetic.main.fragment_monitor.ble_state
import kotlinx.android.synthetic.main.fragment_monitor.hr

class MonitorFragment : Fragment() {

    private val model: MonitorViewModel by viewModels()
    private lateinit var bleInterface: MonitorBleInterface
    private val activityModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bleInterface = MonitorBleInterface()
        bleInterface.setViewModel(model)
        addLiveDataObserver()
        addLiveEventObserver()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_monitor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    @ExperimentalUnsignedTypes
    private fun initView() {}

    @SuppressLint("UseRequireInsteadOfGet")
    private fun connect(b: Bluetooth) {
        this@MonitorFragment.context?.let { bleInterface.connect(it, b.device) }
    }

    private fun addLiveDataObserver() {
        model.deviceName.observe(this) {
            device_sn.text = it
        }

        model.connect.observe(this) {
            if (it) {
                ble_state.setImageResource(R.mipmap.bluetooth_ok)
                battery.visibility = View.VISIBLE
            } else {
                ble_state.setImageResource(R.mipmap.bluetooth_error)
                battery.visibility = View.INVISIBLE
            }
        }

        model.hr.observe(this) {
            hr.text = if (it == 255) {
                "HR: --"
            } else {
                "HR: $it"
            }
        }

        model.pr.observe(this) {
            pr.text = if (it == 255) {
                "PR: --"
            } else {
                "PR: $it"
            }
        }

        model.spo2.observe(this) {
            spo2.text = if (it == 0) {
                "SpO2: --"
            } else {
                "SpO2: $it"
            }
        }

        model.pi.observe(this) {
            pi.text = "PI: ${it/10.0f}"
        }

        model.battery.observe(this) {
            battery.setImageLevel(it)
        }

    }

    private fun addLiveEventObserver() {
        LiveEventBus.get(EventMsgConst.EventDeviceChoosen)
            .observe(this) {
                connect(it as Bluetooth)
            }

        LiveEventBus.get(EventMsgConst.EventMonitorRtdata)
            .observe(this) {
                (it as MonitorRtData).apply {
                    model.battery.value = this.battery

                    model.hr.value = this.hr
                    model.pr.value = this.pr
                    model.spo2.value = this.spo2
                    model.pi.value = this.pi
                }
            }
    }

    companion object {

        @JvmStatic
        fun newInstance() = MonitorFragment()
    }
}