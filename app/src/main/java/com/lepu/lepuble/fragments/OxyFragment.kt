package com.lepu.lepuble.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.lepuble.R
import com.lepu.lepuble.ble.OxyBleInterface
import com.lepu.lepuble.ble.obj.OxyDataController
import com.lepu.lepuble.objs.Bluetooth
import com.lepu.lepuble.vals.EventMsgConst
import com.lepu.lepuble.viewmodel.MainViewModel
import com.lepu.lepuble.viewmodel.OxyViewModel
import com.lepu.lepuble.views.OxyView
import kotlinx.android.synthetic.main.fragment_o2.*
import kotlinx.android.synthetic.main.fragment_o2.battery
import kotlinx.android.synthetic.main.fragment_o2.battery_left_duration
import kotlinx.android.synthetic.main.fragment_o2.ble_state
import kotlinx.android.synthetic.main.fragment_o2.device_sn
import kotlinx.android.synthetic.main.fragment_o2.tv_pr
import kotlin.math.floor

private const val ARG_OXY_DEVICE = "oxy_device"

class OxyFragment : Fragment() {

    private lateinit var bleInterface: OxyBleInterface
    private val model: OxyViewModel by viewModels()
    private val activityModel: MainViewModel by activityViewModels()

    private lateinit var oxyView: OxyView
    private lateinit var viewOxyView: RelativeLayout

    /**
     * rt wave
     */
    private val waveHandler = Handler()

    inner class WaveTask : Runnable {
        override fun run() {
            if (!runWave) {
                return
            }

            val interval: Int = if (OxyDataController.dataRec.size > 250) {
                30
            } else if (OxyDataController.dataRec.size > 150) {
                35
            } else if (OxyDataController.dataRec.size > 75) {
                40
            } else {
                45
            }

            waveHandler.postDelayed(this, interval.toLong())
//            LogUtils.d("DataRec: ${OxyDataController.dataRec.size}, delayed $interval")

            val temp = OxyDataController.draw(5)
            model.dataSrc.value = OxyDataController.feed(model.dataSrc.value, temp)
        }
    }

    private var runWave = false
    private fun startWave() {
        if (runWave) {
            return
        }
        runWave = true
        waveHandler.post(WaveTask())
    }

    private fun stopWave() {
        runWave = false
        OxyDataController.clear()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            device = it.getParcelable(ARG_OXY_DEVICE)
//            LogUtils.d("instance: ${device?.name}")
//            connect()
//        }
        bleInterface = OxyBleInterface()
        bleInterface.setViewModel(model)

        addLiveDataObserver()
        addLiveEventObserver()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_o2, container, false)

        viewOxyView = v.findViewById(R.id.oxi_view)
        viewOxyView.post {
            initOxyView()
        }

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initOxyView() {
        // cal screen
        val dm =resources.displayMetrics
        val index = floor(viewOxyView.width / dm.xdpi * 25.4 / 25 * 125).toInt()
        OxyDataController.maxIndex = index

        val mm2px = 25.4f / dm.xdpi
        OxyDataController.mm2px = mm2px

//        LogUtils.d("max index: $index", "mm2px: $mm2px")

        viewOxyView.measure(0, 0)
        oxyView = OxyView(context)
        viewOxyView.addView(oxyView)

        model.dataSrc.value = OxyDataController.iniDataSrc(index)

        oxyView.visibility = View.GONE
    }

    private fun initUI() {
        get_rt_data.setOnClickListener {
            bleInterface.runRtTask()
        }

        download_file.setOnClickListener {
            model.info.value?.apply {
                bleInterface.downloadFiles(this)
            }
        }
    }

    private fun addLiveDataObserver() {

        activityModel.oxyDeviceName.observe(this, {
            if (it == null) {
                device_sn.text = "no bind device"
            } else {
                device_sn.text = it
            }
        })

        model.dataSrc.observe(this, {
            if (this::oxyView.isInitialized) {
                oxyView.setDataSrc(it)
                oxyView.invalidate()
            }
        })

        model.info.observe(this, {
            device_sn.text = "SN：${it.sn}"
        })

        model.connect.observe(this, {
            if (it) {
                ble_state.setImageResource(R.mipmap.bluetooth_ok)
                oxyView.visibility = View.VISIBLE
                battery.visibility = View.VISIBLE
                battery_left_duration.visibility = View.VISIBLE
                startWave()
            } else {
                ble_state.setImageResource(R.mipmap.bluetooth_error)
                oxyView.visibility = View.INVISIBLE
                battery.visibility = View.INVISIBLE
                battery_left_duration.visibility = View.INVISIBLE
                stopWave()
            }
        })

        model.battery.observe(this, {
            battery.setImageLevel(it)
        })

        model.pr.observe(this, {
            if (it == 0) {
                tv_pr.text = "?"
            } else {
                tv_pr.text = it.toString()
            }
        })
        model.spo2.observe(this, {
            if (it == 0) {
                tv_oxy.text = "?"
            } else {
                tv_oxy.text = it.toString()
            }
        })
//        model.pi.observe(this, {
//            if (it == 0.0f) {
//                tv_pi.text = "?"
//                tv_pi.visibility = View.INVISIBLE
//            } else {
//                tv_pi.text = it.toString()
//                tv_pi.visibility = View.VISIBLE
//            }
//        })
    }



    private fun addLiveEventObserver() {
        LiveEventBus.get(EventMsgConst.EventDeviceChoosen)
                .observe(this, {
                    connect(it as Bluetooth)
                })
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun connect(b: Bluetooth) {
        this@OxyFragment.context?.let { bleInterface.connect(it, b.device) }
    }

    companion object {
        @JvmStatic
        fun newInstance(b: Bluetooth) =
            OxyFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_OXY_DEVICE, b)
                }
            }

        @JvmStatic
        fun newInstance() = OxyFragment()
    }
}