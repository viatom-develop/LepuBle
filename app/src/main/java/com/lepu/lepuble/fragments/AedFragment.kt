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
import com.lepu.lepuble.ble.AedBleInterface
import com.lepu.lepuble.ble.Er1BleInterface
import com.lepu.lepuble.objs.Bluetooth
import com.lepu.lepuble.utils.HexString
import com.lepu.lepuble.vals.EventMsgConst
import com.lepu.lepuble.viewmodel.AedViewModel
import com.lepu.lepuble.viewmodel.Er1ViewModel
import com.lepu.lepuble.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_aed.*
import kotlinx.android.synthetic.main.fragment_er1.*
import kotlinx.android.synthetic.main.fragment_er1.battery
import kotlinx.android.synthetic.main.fragment_er1.battery_left_duration
import kotlinx.android.synthetic.main.fragment_er1.ble_state
import kotlinx.android.synthetic.main.fragment_er1.device_sn
import kotlinx.android.synthetic.main.fragment_er1.speed


class AedFragment : Fragment() {

    private lateinit var bleInterface : AedBleInterface

    private val model: AedViewModel by viewModels()
    private val activityModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bleInterface = AedBleInterface()
        bleInterface.setViewModel(model)
        addLiveDataObserver()
        addLiveEventObserver()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_aed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        send_cmd.setOnClickListener {
            bleInterface.configSerial(
                adult_f.text.toString().toInt(),
                adult_s.text.toString().toInt(),
                adult_t.text.toString().toInt(),
                child_f.text.toString().toInt(),
                child_s.text.toString().toInt(),
                child_t.text.toString().toInt()
            )
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun connect(b: Bluetooth) {
        this@AedFragment.context?.let { bleInterface.connect(it, b.device) }
    }

    @ExperimentalUnsignedTypes
    private fun addLiveDataObserver() {
        activityModel.er1DeviceName.observe(this, {
            if (it == null) {
                device_sn.text = "no bind device"
            } else {
                device_sn.text = it
            }
        })


        model.aed.observe(this, {
            device_sn.text = "SN：${it.sn}"
        })

        model.connect.observe(this, {
            if (it) {
                ble_state.setImageResource(R.mipmap.bluetooth_ok)
                battery_left_duration.visibility = View.VISIBLE
            } else {
                ble_state.setImageResource(R.mipmap.bluetooth_error)
                battery_left_duration.visibility = View.INVISIBLE
            }
        })

        model.battery.observe(this, {
            battery.setImageLevel(it)
        })

        model.speed.observe(this, {
            speed.text = "$it kb/s"
        })
    }

    /**
     * observe LiveDataBus
     * receive from KcaBleInterface
     * 考虑直接从interface来控制，不需要所有的都传递
     */
    private fun addLiveEventObserver() {
        LiveEventBus.get(EventMsgConst.EventDeviceChoosen)
            .observe(this, {
                connect(it as Bluetooth)
            })

        LiveEventBus.get(EventMsgConst.EventCommonMsg)
            .observe(this, {
                download_progress.text = it as String
            })

        LiveEventBus.get(EventMsgConst.EventMsgSendCmd)
            .observe(this, {
                bleInterface.sendCmd(HexString.hexToBytes(it as String))
            })
    }

    companion object {
        @JvmStatic
        fun newInstance() = AedFragment()
    }
}