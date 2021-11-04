package com.lepu.lepuble.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.annotation.FloatRange
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.afollestad.materialdialogs.MaterialDialog
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.lepuble.R
import com.lepu.lepuble.ble.Am300bBleInterface
import com.lepu.lepuble.ble.cmd.Am300Obj
import com.lepu.lepuble.ble.cmd.AmResponse
import com.lepu.lepuble.objs.Bluetooth
import com.lepu.lepuble.utils.HexString
import com.lepu.lepuble.vals.EventMsgConst
import com.lepu.lepuble.viewmodel.Am300bViewModel
import com.lepu.lepuble.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_am300b.*
import java.util.*
import kotlin.concurrent.schedule

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

        check_ab.setOnCheckedChangeListener { buttonView, isChecked ->
            model.is_ab.value = isChecked

            if (isChecked) {
                model.frequency_b.value = model.frequency_a.value
                model.bandwidth_b.value = model.bandwidth_a.value
                model.raise_b.value = model.raise_a.value
                model.fall_b.value = model.fall_a.value
                model.duration_b.value = model.duration_a.value
                model.rest_b.value = model.rest_a.value

                updateParams(3)
            }

        }

        sp_a_freq.setOnClickListener {
            val list = (1 .. 120).toMutableList()
            val picker = OptionsPickerBuilder(this.activity) {options1, options2, options3, v ->
                model.frequency_a.value = list[options1]
                if (model.is_ab.value!!) {
                    model.frequency_b.value = list[options1]
                    updateParams(3)
                } else {
                    updateParams(1)
                }
            }.build<Int>()
            picker.setPicker(list)
            picker.show()
        }
        sp_b_freq.setOnClickListener {
            val list = (1 .. 120).toMutableList()
            val picker = OptionsPickerBuilder(this.activity) {options1, options2, options3, v ->
                model.frequency_b.value = list[options1]
                if (model.is_ab.value!!) {
                    model.frequency_b.value = list[options1]
                    updateParams(3)
                } else {
                    updateParams(2)
                }
            }.build<Int>()
            picker.setPicker(list)
            picker.show()
        }

        sp_a_bandwidth.setOnClickListener {
            val list = (50 .. 450).step(50).toMutableList()
            val picker = OptionsPickerBuilder(this.activity) {options1, options2, options3, v ->
                model.bandwidth_a.value = list[options1]
                if (model.is_ab.value!!) {
                    model.bandwidth_b.value = list[options1]
                    updateParams(3)
                } else {
                    updateParams(1)
                }
            }.build<Int>()
            picker.setPicker(list)
            picker.show()
        }
        sp_b_bandwidth.setOnClickListener {
            val list = (50 .. 450).step(50).toMutableList()
            val picker = OptionsPickerBuilder(this.activity) {options1, options2, options3, v ->
                model.bandwidth_b.value = list[options1]
                if (model.is_ab.value!!) {
                    model.bandwidth_a.value = list[options1]
                    updateParams(3)
                } else {
                    updateParams(2)
                }
            }.build<Int>()
            picker.setPicker(list)
            picker.show()
        }

        sp_a_raise.setOnClickListener {
            val list = mutableListOf<Float>()
            for (i in 0 .. 180) {
                list.add(i/10.0f)
            }
            val picker = OptionsPickerBuilder(this.activity) {options1, options2, options3, v ->
                model.raise_a.value = list[options1]
                if (model.is_ab.value!!) {
                    model.raise_b.value = list[options1]
                    updateParams(3)
                } else {
                    updateParams(1)
                }
            }.build<Float>()

            picker.setPicker(list)
            picker.show()
        }
        sp_b_raise.setOnClickListener {
            val list = mutableListOf<Float>()
            for (i in 0 .. 180) {
                list.add(i/10.0f)
            }
            val picker = OptionsPickerBuilder(this.activity) {options1, options2, options3, v ->
                model.raise_b.value = list[options1]
                if (model.is_ab.value!!) {
                    model.raise_a.value = list[options1]
                    updateParams(3)
                } else {
                    updateParams(2)
                }
            }.build<Float>()

            picker.setPicker(list)
            picker.show()
        }

        sp_a_fall.setOnClickListener {
            val list = mutableListOf<Float>()
            for (i in 0 .. 180) {
                list.add(i/10.0f)
            }
            val picker = OptionsPickerBuilder(this.activity) {options1, options2, options3, v ->
                model.fall_a.value = list[options1]
                if (model.is_ab.value!!) {
                    model.fall_b.value = list[options1]
                    updateParams(3)
                } else {
                    updateParams(1)
                }
            }.build<Float>()

            picker.setPicker(list)
            picker.show()
        }
        sp_b_fall.setOnClickListener {
            val list = mutableListOf<Float>()
            for (i in 0 .. 180) {
                list.add(i/10.0f)
            }
            val picker = OptionsPickerBuilder(this.activity) {options1, options2, options3, v ->
                model.fall_b.value = list[options1]
                if (model.is_ab.value!!) {
                    model.fall_a.value = list[options1]
                    updateParams(3)
                } else {
                    updateParams(2)
                }
            }.build<Float>()

            picker.setPicker(list)
            picker.show()
        }

        sp_a_duration.setOnClickListener {
            val list = (0 .. 60).toMutableList()
            val picker = OptionsPickerBuilder(this.activity) {options1, options2, options3, v ->
                model.duration_a.value = list[options1]
                if (model.is_ab.value!!) {
                    model.duration_b.value = list[options1]
                    updateParams(3)
                } else {
                    updateParams(1)
                }
            }.build<Int>()
            picker.setPicker(list)
            picker.show()
        }
        sp_b_duration.setOnClickListener {
            val list = (0 .. 60).toMutableList()
            val picker = OptionsPickerBuilder(this.activity) {options1, options2, options3, v ->
                model.duration_b.value = list[options1]
                if (model.is_ab.value!!) {
                    model.duration_a.value = list[options1]
                    updateParams(3)
                } else {
                    updateParams(2)
                }
            }.build<Int>()
            picker.setPicker(list)
            picker.show()
        }

        sp_a_reset.setOnClickListener {
            val list = (0 .. 60).toMutableList()
            val picker = OptionsPickerBuilder(this.activity) {options1, options2, options3, v ->
                model.rest_a.value = list[options1]
                if (model.is_ab.value!!) {
                    model.rest_b.value = list[options1]
                    updateParams(3)
                } else {
                    updateParams(1)
                }
            }.build<Int>()
            picker.setPicker(list)
            picker.show()
        }
        sp_b_reset.setOnClickListener {
            val list = (0 .. 60).toMutableList()
            val picker = OptionsPickerBuilder(this.activity) {options1, options2, options3, v ->
                model.rest_b.value = list[options1]
                if (model.is_ab.value!!) {
                    model.rest_a.value = list[options1]
                    updateParams(3)
                } else {
                    updateParams(2)
                }
            }.build<Int>()
            picker.setPicker(list)
            picker.show()
        }

        sp_intensity_a.setOnClickListener {
            val list = (0 .. 90).toMutableList()
            val picker = OptionsPickerBuilder(this.activity) {options1, options2, options3, v ->
                model.channelA.value = list[options1]
                updateIntensity(model.channelA.value!!, 1)
            }.build<Int>()
            picker.setPicker(list)
            picker.show()
        }
        sp_intensity_b.setOnClickListener {
            val list = (0 .. 90).toMutableList()
            val picker = OptionsPickerBuilder(this.activity) {options1, options2, options3, v ->
                model.channelB.value = list[options1]
                updateIntensity(model.channelB.value!!, 2)
            }.build<Int>()
            picker.setPicker(list)
            picker.show()
        }

        intensity_start_a.setOnClickListener {
//            if (model.is_ab.value!!) {
//                bleInterface.startIntensity(3)
//            } else {
                bleInterface.startIntensity(1)
//            }
        }
        intensity_start_b.setOnClickListener {
//            if (model.is_ab.value!!) {
//                bleInterface.startIntensity(3)
//            } else {
                bleInterface.startIntensity(2)
//            }
        }

        intensity_start_ab.setOnClickListener {
            bleInterface.startIntensity(3)
        }

        intensity_end.setOnClickListener {
            bleInterface.endIntensity(model.channel.value!!)
        }

        btn1.setOnClickListener {
            bleInterface.queryParam()
        }
        btn2.setOnClickListener {
            bleInterface.queryIntensity()
        }
        btn3.setOnClickListener {
            bleInterface.queryWorkingStatus()
        }
    }

    private fun updateIntensity(value: Int, channel: Int) {
        bleInterface.setIntensity(value, channel)
    }

    private fun updateParams(channel: Int) {

        when(channel) {
            2->bleInterface.setParam(
                channel,
                model.frequency_b.value!!,
                model.bandwidth_b.value!!,
                model.raise_b.value!!,
                model.fall_b.value!!,
                model.duration_b.value!!,
                model.rest_b.value!!
            )
            else->bleInterface.setParam(
                channel,
                model.frequency_a.value!!,
                model.bandwidth_a.value!!,
                model.raise_a.value!!,
                model.fall_a.value!!,
                model.duration_a.value!!,
                model.rest_a.value!!
            )
        }

        model.channelA.value = 0
        model.channelB.value = 0
    }

    private fun initParams() {
        model.is_ab.value = false
        model.channel.value = 3
        model.frequency_a.value = 100
        model.frequency_b.value = 100
        model.bandwidth_a.value = 200
        model.bandwidth_b.value = 200
        model.raise_a.value = 1.0f
        model.raise_b.value = 1.0f
        model.fall_a.value = 1.0f
        model.fall_b.value = 1.0f
        model.duration_a.value = 5
        model.duration_b.value = 5
        model.rest_a.value = 5
        model.rest_b.value = 5
        model.channelA.value = 0
        model.channelB.value = 0

        /**
         * test
         */
//        model.emgLead.value = Am300Obj.EmgLeadState(byteArrayOf(0x00, 0x01))
//        Timer().schedule(1000) {model.emgLead.postValue(Am300Obj.EmgLeadState(byteArrayOf(0x02, 0x00)))}
//        Timer().schedule(3000) {model.emgLead.postValue(Am300Obj.EmgLeadState(byteArrayOf(0x03, 0x01)))}
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
//
        model.emgLead.observe(this, {
//            if (it.electrode_lead) {
//                electrode_lead.visibility = View.INVISIBLE
//            } else {
//                electrode_lead.visibility = View.VISIBLE
//            }
//
//            if (it.probe_lead) {
//                probe_lead.visibility = View.INVISIBLE
//            } else {
//                probe_lead.visibility = View.VISIBLE
//            }

            electrode_lead.text = if (it.electrode1) {
                ""
            } else {
                "电极1脱落"
            }

            var probe = ""
            if (!it.probeA) {
                probe += "A脱落"
            }
            if (!it.probeB) {
                probe += "B脱落"
            }
            probe_lead.text = probe
        })

//        model.channel.observe(this, {
//            val list = listOf<String>("A 通道", "B 通道", "A+B 通道")
//            sp_channel.text = list[it-1]
//        })

        model.frequency_a.observe(this, {
            sp_a_freq.text = "$it Hz"
        })
        model.bandwidth_a.observe(this, {
            sp_a_bandwidth.text = "$it us"
        })
        model.raise_a.observe(this, {
            sp_a_raise.text = "$it s"
        })
        model.fall_a.observe(this, {
            sp_a_fall.text = "$it s"
        })
        model.duration_a.observe(this, {
            sp_a_duration.text = "$it s"
        })
        model.rest_a.observe(this, {
            sp_a_reset.text = "$it s"
        })

        model.frequency_b.observe(this, {
            sp_b_freq.text = "$it Hz"
        })
        model.bandwidth_b.observe(this, {
            sp_b_bandwidth.text = "$it us"
        })
        model.raise_b.observe(this, {
            sp_b_raise.text = "$it s"
        })
        model.fall_b.observe(this, {
            sp_b_fall.text = "$it s"
        })
        model.duration_b.observe(this, {
            sp_b_duration.text = "$it s"
        })
        model.rest_b.observe(this, {
            sp_b_reset.text = "$it s"
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

        LiveEventBus.get(EventMsgConst.EventMsgSendCmd)
            .observe(this, {
                bleInterface.sendCmd(HexString.hexToBytes(it as String))
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