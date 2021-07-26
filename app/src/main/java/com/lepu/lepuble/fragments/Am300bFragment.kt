package com.lepu.lepuble.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.FloatRange
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.lepuble.R
import com.lepu.lepuble.ble.Am300bBleInterface
import com.lepu.lepuble.objs.Bluetooth
import com.lepu.lepuble.vals.EventMsgConst
import com.lepu.lepuble.viewmodel.Am300bViewModel
import com.lepu.lepuble.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_am300b.*

class Am300bFragment : Fragment() {

    private lateinit var bleInterface: Am300bBleInterface

    private val model: Am300bViewModel by viewModels()
    private val activityModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bleInterface = Am300bBleInterface()
        bleInterface.setViewModel(model)
        addLiveDataObserver()
        addLiveEventObserver()

        initParams()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_am300b, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        emg_start.setOnClickListener {
            bleInterface.startEmg()
        }
        emg_end.setOnClickListener {
            bleInterface.endEmg()
        }

        sp_freq.setOnClickListener {
            val list = (1 .. 120).toMutableList()
            val picker = OptionsPickerBuilder(this.activity) {options1, options2, options3, v ->
                model.frequency.value = list[options1]
            }.build<Int>()
            picker.setPicker(list)
            picker.show()
            updateParams()
        }
        sp_bandwidth.setOnClickListener {
            val list = (50 .. 450).step(50).toMutableList()
            val picker = OptionsPickerBuilder(this.activity) {options1, options2, options3, v ->
                model.bandwidth.value = list[options1]
            }.build<Int>()
            picker.setPicker(list)
            picker.show()
            updateParams()
        }
        sp_raise.setOnClickListener {
            val list = mutableListOf<Float>()
            for (i in 0 .. 180) {
                list.add(i/10.0f)
            }
            val picker = OptionsPickerBuilder(this.activity) {options1, options2, options3, v ->
                model.raise.value = list[options1]
            }.build<Float>()

            picker.setPicker(list)
            picker.show()
            updateParams()
        }
        sp_fall.setOnClickListener {
            val list = mutableListOf<Float>()
            for (i in 0 .. 180) {
                list.add(i/10.0f)
            }
            val picker = OptionsPickerBuilder(this.activity) {options1, options2, options3, v ->
                model.fall.value = list[options1]
            }.build<Float>()

            picker.setPicker(list)
            picker.show()
            updateParams()
        }
        sp_duration.setOnClickListener {
            val list = (0 .. 60).toMutableList()
            val picker = OptionsPickerBuilder(this.activity) {options1, options2, options3, v ->
                model.duration.value = list[options1]
            }.build<Int>()
            picker.setPicker(list)
            picker.show()
            updateParams()
        }
        sp_reset.setOnClickListener {
            val list = (0 .. 60).toMutableList()
            val picker = OptionsPickerBuilder(this.activity) {options1, options2, options3, v ->
                model.rest.value = list[options1]
            }.build<Int>()
            picker.setPicker(list)
            picker.show()
            updateParams()
        }
        sp_intensity_a.setOnClickListener {
            val list = (0 .. 90).toMutableList()
            val picker = OptionsPickerBuilder(this.activity) {options1, options2, options3, v ->
                model.channelA.value = list[options1]
            }.build<Int>()
            picker.setPicker(list)
            picker.show()
            updateIntensity()
        }
        sp_intensity_b.setOnClickListener {
            val list = (0 .. 90).toMutableList()
            val picker = OptionsPickerBuilder(this.activity) {options1, options2, options3, v ->
                model.channelB.value = list[options1]
            }.build<Int>()
            picker.setPicker(list)
            picker.show()
            updateIntensity()
        }

        intensity_start.setOnClickListener {
            bleInterface.startIntensity()
        }

        intensity_end.setOnClickListener {
            bleInterface.endIntensity()
        }
    }

    private fun updateIntensity() {
        bleInterface.setIntensity(model.channelA.value!!, model.channelB.value!!)
    }

    private fun updateParams() {
        bleInterface.setParam(
            model.frequency.value!!,
            model.bandwidth.value!!,
            model.raise.value!!,
            model.fall.value!!,
            model.duration.value!!,
            model.rest.value!!
        )
    }

    private fun initParams() {
        model.frequency.value = 100
        model.bandwidth.value = 200
        model.raise.value = 1.0f
        model.fall.value = 1.0f
        model.duration.value = 5
        model.rest.value = 5
        model.channelA.value = 10
        model.channelB.value = 10
    }

    private fun addLiveDataObserver() {
        model.sn.observe(this, {
            if (it == null) {
                device_sn.text = "no bind device"
            } else {
                device_sn.text = it
            }
        })
        model.version.observe(this, {
            version.text = it
        })
        model.battery.observe(this, {
            battery.setImageLevel(it)
        })

        model.connect.observe(this, {
            if (it) {
                ble_state.setImageResource(R.mipmap.bluetooth_ok)
                battery.visibility = View.VISIBLE
            } else {
                ble_state.setImageResource(R.mipmap.bluetooth_error)
                battery.visibility = View.INVISIBLE
            }
        })

        model.emgState.observe(this, {
            if (it) {
                Toast.makeText(context, "开始EMG上传", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "停止EMG上传", Toast.LENGTH_SHORT).show()
            }
        })

        model.emgPkg.observe(this, {
            channel_a.text = "A: ${it.a}"
            channel_b.text = "B: ${it.b}"
        })

        model.emgLead.observe(this, {
            if (it.electrode_lead) {
                electrode_lead.visibility = View.INVISIBLE
            } else {
                electrode_lead.visibility = View.VISIBLE
            }

            if (it.probe_lead) {
                probe_lead.visibility = View.INVISIBLE
            } else {
                probe_lead.visibility = View.VISIBLE
            }
        })

        model.frequency.observe(this, {
            sp_freq.text = "$it Hz"
        })
        model.bandwidth.observe(this, {
            sp_bandwidth.text = "$it us"
        })
        model.raise.observe(this, {
            sp_raise.text = "$it s"
        })
        model.fall.observe(this, {
            sp_fall.text = "$it s"
        })
        model.duration.observe(this, {
            sp_duration.text = "$it s"
        })
        model.rest.observe(this, {
            sp_reset.text = "$it s"
        })
        model.channelA.observe(this, {
            sp_intensity_a.text = "$it mA"
        })
        model.channelB.observe(this, {
            sp_intensity_b.text = "$it mA"
        })
    }


    private fun addLiveEventObserver() {
        LiveEventBus.get(EventMsgConst.EventDeviceChoosen)
            .observe(this, {
                connect(it as Bluetooth)
            })
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun connect(b: Bluetooth) {
        this@Am300bFragment.context?.let { bleInterface.connect(it, b.device) }
    }

    companion object {
        @JvmStatic
        fun newInstance() = Am300bFragment()
    }
}