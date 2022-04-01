package com.lepu.lepuble.fragments

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.lepuble.R
import com.lepu.lepuble.activity.About
import com.lepu.lepuble.ble.Er3BleInterface
import com.lepu.lepuble.ble.obj.EcgDataController
import com.lepu.lepuble.objs.Bluetooth
import com.lepu.lepuble.vals.EventMsgConst
import com.lepu.lepuble.viewmodel.Er3ViewModel
import com.lepu.lepuble.viewmodel.MainViewModel
import com.lepu.lepuble.views.EcgBkg12
import com.lepu.lepuble.views.EcgView
import com.lepu.lepuble.views.EcgView12
import kotlinx.android.synthetic.main.fragment_er3.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor

class Er3Fragment: Fragment() {
    private lateinit var bleInterface: Er3BleInterface

    private val model: Er3ViewModel by viewModels()
    private val activityModel: MainViewModel by activityViewModels()

    // 待优化
    private lateinit var ecgBkg1: EcgBkg12
    private lateinit var ecgBkg2: EcgBkg12
    private lateinit var ecgBkg3: EcgBkg12
    private lateinit var ecgBkg4: EcgBkg12
    private lateinit var ecgBkg5: EcgBkg12
    private lateinit var ecgBkg6: EcgBkg12
    private lateinit var ecgBkg7: EcgBkg12
    private lateinit var ecgBkg8: EcgBkg12
    private lateinit var ecgBkg9: EcgBkg12
    private lateinit var ecgBkg10: EcgBkg12
    private lateinit var ecgBkg11: EcgBkg12
    private lateinit var ecgBkg12: EcgBkg12

    private lateinit var ecgView1: EcgView12
    private lateinit var ecgView2: EcgView12
    private lateinit var ecgView3: EcgView12
    private lateinit var ecgView4: EcgView12
    private lateinit var ecgView5: EcgView12
    private lateinit var ecgView6: EcgView12
    private lateinit var ecgView7: EcgView12
    private lateinit var ecgView8: EcgView12
    private lateinit var ecgView9: EcgView12
    private lateinit var ecgView10: EcgView12
    private lateinit var ecgView11: EcgView12
    private lateinit var ecgView12: EcgView12

    private lateinit var viewEcgBkg1: RelativeLayout
    private lateinit var viewEcgBkg2: RelativeLayout
    private lateinit var viewEcgBkg3: RelativeLayout
    private lateinit var viewEcgBkg4: RelativeLayout
    private lateinit var viewEcgBkg5: RelativeLayout
    private lateinit var viewEcgBkg6: RelativeLayout
    private lateinit var viewEcgBkg7: RelativeLayout
    private lateinit var viewEcgBkg8: RelativeLayout
    private lateinit var viewEcgBkg9: RelativeLayout
    private lateinit var viewEcgBkg10: RelativeLayout
    private lateinit var viewEcgBkg11: RelativeLayout
    private lateinit var viewEcgBkg12: RelativeLayout

    private lateinit var viewEcgView1: RelativeLayout
    private lateinit var viewEcgView2: RelativeLayout
    private lateinit var viewEcgView3: RelativeLayout
    private lateinit var viewEcgView4: RelativeLayout
    private lateinit var viewEcgView5: RelativeLayout
    private lateinit var viewEcgView6: RelativeLayout
    private lateinit var viewEcgView7: RelativeLayout
    private lateinit var viewEcgView8: RelativeLayout
    private lateinit var viewEcgView9: RelativeLayout
    private lateinit var viewEcgView10: RelativeLayout
    private lateinit var viewEcgView11: RelativeLayout
    private lateinit var viewEcgView12: RelativeLayout

    private var device: Bluetooth? = null

    private var dialog: ProgressDialog? = null

    /**
     * rt wave
     */
    private val waveHandler = Handler()

    inner class WaveTask : Runnable {
        override fun run() {
            if (!runWave) {
                return
            }

            val interval: Int = when {
                EcgDataController.dataRec.size > 250*8*2 -> {
                    30
                }
                EcgDataController.dataRec.size > 150*8*2 -> {
                    35
                }
                EcgDataController.dataRec.size > 75*8*2 -> {
                    40
                }
                else -> {
                    45
                }
            }

            waveHandler.postDelayed(this, interval.toLong())
//            LogUtils.d("DataRec: ${Er1DataController.dataRec.size}, delayed $interval")

            EcgDataController.draw(10)
            /**
             * update viewModel
             */
            model.dataSrc1.value = EcgDataController.src1
            model.dataSrc2.value = EcgDataController.src2
            model.dataSrc3.value = EcgDataController.src3
            model.dataSrc4.value = EcgDataController.src4
            model.dataSrc5.value = EcgDataController.src5
            model.dataSrc6.value = EcgDataController.src6
            model.dataSrc7.value = EcgDataController.src7
            model.dataSrc8.value = EcgDataController.src8
            model.dataSrc9.value = EcgDataController.src9
            model.dataSrc10.value = EcgDataController.src10
            model.dataSrc11.value = EcgDataController.src11
            model.dataSrc12.value = EcgDataController.src12
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
        EcgDataController.clear()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            device = it.getParcelable(ARG_ER1_DEVICE)
//            LogUtils.d("instance: ${device?.name}")
//            connect()
//        }
        bleInterface = Er3BleInterface()
        bleInterface.setViewModel(model)
        addLiveDataObserver()
        addLiveEventObserver()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_er3, container, false)

        // add view
        viewEcgBkg1 = v.findViewById<RelativeLayout>(R.id.ecg_bkg_1)
        viewEcgBkg2 = v.findViewById<RelativeLayout>(R.id.ecg_bkg_2)
        viewEcgBkg3 = v.findViewById<RelativeLayout>(R.id.ecg_bkg_3)
        viewEcgBkg4 = v.findViewById<RelativeLayout>(R.id.ecg_bkg_4)
        viewEcgBkg5 = v.findViewById<RelativeLayout>(R.id.ecg_bkg_5)
        viewEcgBkg6 = v.findViewById<RelativeLayout>(R.id.ecg_bkg_6)
        viewEcgBkg7 = v.findViewById<RelativeLayout>(R.id.ecg_bkg_7)
        viewEcgBkg8 = v.findViewById<RelativeLayout>(R.id.ecg_bkg_8)
        viewEcgBkg9 = v.findViewById<RelativeLayout>(R.id.ecg_bkg_9)
        viewEcgBkg10 = v.findViewById<RelativeLayout>(R.id.ecg_bkg_10)
        viewEcgBkg11 = v.findViewById<RelativeLayout>(R.id.ecg_bkg_11)
        viewEcgBkg12 = v.findViewById<RelativeLayout>(R.id.ecg_bkg_12)

        viewEcgView1 = v.findViewById<RelativeLayout>(R.id.ecg_view_1)
        viewEcgView2 = v.findViewById<RelativeLayout>(R.id.ecg_view_2)
        viewEcgView3 = v.findViewById<RelativeLayout>(R.id.ecg_view_3)
        viewEcgView4 = v.findViewById<RelativeLayout>(R.id.ecg_view_4)
        viewEcgView5 = v.findViewById<RelativeLayout>(R.id.ecg_view_5)
        viewEcgView6 = v.findViewById<RelativeLayout>(R.id.ecg_view_6)
        viewEcgView7 = v.findViewById<RelativeLayout>(R.id.ecg_view_7)
        viewEcgView8 = v.findViewById<RelativeLayout>(R.id.ecg_view_8)
        viewEcgView9 = v.findViewById<RelativeLayout>(R.id.ecg_view_9)
        viewEcgView10 = v.findViewById<RelativeLayout>(R.id.ecg_view_10)
        viewEcgView11 = v.findViewById<RelativeLayout>(R.id.ecg_view_11)
        viewEcgView12 = v.findViewById<RelativeLayout>(R.id.ecg_view_12)

        viewEcgBkg12.post {
            initEcgView()
        }

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()

//        startWave()
    }

    @ExperimentalUnsignedTypes
    private fun initView() {
//        get_file_list.setOnClickListener {
//            bleInterface.getFileList()
//        }
//
//        /**
//         * 默认下载第一条数据
//         */
//        download_file.setOnClickListener {
//            if (bleInterface.fileList == null || bleInterface.fileList!!.size == 0) {
//                Toast.makeText(activity, "please get file list at first or file list is null", Toast.LENGTH_SHORT).show()
//            } else {
//                val name = bleInterface.fileList!!.fileList[0]
//                bleInterface.downloadFile(name)
//            }
//
//        }

        get_rt_data.setOnClickListener {
            bleInterface.runRtTask()
            startWave()
        }

        about.setOnClickListener {
            val i = Intent(activity, About::class.java)
            i.putExtra("er3", model.er3.value)
            startActivity(i)
        }

        planets_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                bleInterface.setConfig(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }
        }

        factory_reset.setOnClickListener {
            bleInterface.factoryReset()
        }
    }

    private fun initEcgView() {
        // cal screen
        val dm =resources.displayMetrics
        val index = floor(viewEcgBkg1.width / dm.xdpi * 25.4 / 25 * 250).toInt()
        EcgDataController.maxIndex = index

        val mm2px = 25.4f / dm.xdpi
        EcgDataController.mm2px = mm2px

//        LogUtils.d("max index: $index", "mm2px: $mm2px")

        viewEcgBkg1.measure(0, 0)
        ecgBkg1 = EcgBkg12(context)
        viewEcgBkg1.addView(ecgBkg1)

        viewEcgView1.measure(0, 0)
        ecgView1 = EcgView12(context)
        viewEcgView1.addView(ecgView1)

        viewEcgBkg2.measure(0, 0)
        ecgBkg2 = EcgBkg12(context)
        viewEcgBkg2.addView(ecgBkg2)

        viewEcgView2.measure(0, 0)
        ecgView2 = EcgView12(context)
        viewEcgView2.addView(ecgView2)

        viewEcgBkg3.measure(0, 0)
        ecgBkg3 = EcgBkg12(context)
        viewEcgBkg3.addView(ecgBkg3)

        viewEcgView3.measure(0, 0)
        ecgView3 = EcgView12(context)
        viewEcgView3.addView(ecgView3)

        viewEcgBkg4.measure(0, 0)
        ecgBkg4 = EcgBkg12(context)
        viewEcgBkg4.addView(ecgBkg4)

        viewEcgView4.measure(0, 0)
        ecgView4 = EcgView12(context)
        viewEcgView4.addView(ecgView4)

        viewEcgBkg5.measure(0, 0)
        ecgBkg5 = EcgBkg12(context)
        viewEcgBkg5.addView(ecgBkg5)

        viewEcgView5.measure(0, 0)
        ecgView5 = EcgView12(context)
        viewEcgView5.addView(ecgView5)

        viewEcgBkg6.measure(0, 0)
        ecgBkg6 = EcgBkg12(context)
        viewEcgBkg6.addView(ecgBkg6)

        viewEcgView6.measure(0, 0)
        ecgView6 = EcgView12(context)
        viewEcgView6.addView(ecgView6)

        viewEcgBkg7.measure(0, 0)
        ecgBkg7 = EcgBkg12(context)
        viewEcgBkg7.addView(ecgBkg7)

        viewEcgView7.measure(0, 0)
        ecgView7 = EcgView12(context)
        viewEcgView7.addView(ecgView7)

        viewEcgBkg8.measure(0, 0)
        ecgBkg8 = EcgBkg12(context)
        viewEcgBkg8.addView(ecgBkg8)

        viewEcgView8.measure(0, 0)
        ecgView8 = EcgView12(context)
        viewEcgView8.addView(ecgView8)

        viewEcgBkg9.measure(0, 0)
        ecgBkg9 = EcgBkg12(context)
        viewEcgBkg9.addView(ecgBkg9)

        viewEcgView9.measure(0, 0)
        ecgView9 = EcgView12(context)
        viewEcgView9.addView(ecgView9)

        viewEcgBkg10.measure(0, 0)
        ecgBkg10 = EcgBkg12(context)
        viewEcgBkg10.addView(ecgBkg10)

        viewEcgView10.measure(0, 0)
        ecgView10 = EcgView12(context)
        viewEcgView10.addView(ecgView10)

        viewEcgBkg11.measure(0, 0)
        ecgBkg11 = EcgBkg12(context)
        viewEcgBkg11.addView(ecgBkg11)

        viewEcgView11.measure(0, 0)
        ecgView11 = EcgView12(context)
        viewEcgView11.addView(ecgView11)

        viewEcgBkg12.measure(0, 0)
        ecgBkg12 = EcgBkg12(context)
        viewEcgBkg12.addView(ecgBkg12)

        viewEcgView12.measure(0, 0)
        ecgView12 = EcgView12(context)
        viewEcgView12.addView(ecgView12)

        ecgViewVisible(false)
    }

    private fun ecgViewVisible(b: Boolean) {
        if (b) {
            ecg_view_1.visibility = View.VISIBLE
            ecg_view_2.visibility = View.VISIBLE
            ecg_view_3.visibility = View.VISIBLE
            ecg_view_4.visibility = View.VISIBLE
            ecg_view_5.visibility = View.VISIBLE
            ecg_view_6.visibility = View.VISIBLE
            ecg_view_7.visibility = View.VISIBLE
            ecg_view_8.visibility = View.VISIBLE
            ecg_view_9.visibility = View.VISIBLE
            ecg_view_10.visibility = View.VISIBLE
            ecg_view_11.visibility = View.VISIBLE
            ecg_view_12.visibility = View.VISIBLE
        } else {
            ecg_view_1.visibility = View.GONE
            ecg_view_2.visibility = View.GONE
            ecg_view_3.visibility = View.GONE
            ecg_view_4.visibility = View.GONE
            ecg_view_5.visibility = View.GONE
            ecg_view_6.visibility = View.GONE
            ecg_view_7.visibility = View.GONE
            ecg_view_8.visibility = View.GONE
            ecg_view_9.visibility = View.GONE
            ecg_view_10.visibility = View.GONE
            ecg_view_11.visibility = View.GONE
            ecg_view_12.visibility = View.GONE
        }
    }

    // Er1ViewModel
    private fun addLiveDataObserver(){

        activityModel.er1DeviceName.observe(this) {
            if (it == null) {
                device_sn.text = "no bind device"
            } else {
                device_sn.text = it
            }
        }

        model.dataSrc1.observe(this) {
            if (this::ecgView1.isInitialized) {
                ecgView1.setDataSrc(it)
                ecgView1.invalidate()
            }
        }
        model.dataSrc2.observe(this) {
            if (this::ecgView2.isInitialized) {
                ecgView2.setDataSrc(it)
                ecgView2.invalidate()
            }
        }
        model.dataSrc3.observe(this) {
            if (this::ecgView3.isInitialized) {
                ecgView3.setDataSrc(it)
                ecgView3.invalidate()
            }
        }
        model.dataSrc4.observe(this) {
            if (this::ecgView4.isInitialized) {
                ecgView4.setDataSrc(it)
                ecgView4.invalidate()
            }
        }
        model.dataSrc5.observe(this) {
            if (this::ecgView5.isInitialized) {
                ecgView5.setDataSrc(it)
                ecgView5.invalidate()
            }
        }
        model.dataSrc6.observe(this) {
            if (this::ecgView6.isInitialized) {
                ecgView6.setDataSrc(it)
                ecgView6.invalidate()
            }
        }
        model.dataSrc7.observe(this) {
            if (this::ecgView7.isInitialized) {
                ecgView7.setDataSrc(it)
                ecgView7.invalidate()
            }
        }
        model.dataSrc8.observe(this) {
            if (this::ecgView8.isInitialized) {
                ecgView8.setDataSrc(it)
                ecgView8.invalidate()
            }
        }
        model.dataSrc9.observe(this) {
            if (this::ecgView9.isInitialized) {
                ecgView9.setDataSrc(it)
                ecgView9.invalidate()
            }
        }
        model.dataSrc10.observe(this) {
            if (this::ecgView10.isInitialized) {
                ecgView10.setDataSrc(it)
                ecgView10.invalidate()
            }
        }
        model.dataSrc11.observe(this) {
            if (this::ecgView11.isInitialized) {
                ecgView11.setDataSrc(it)
                ecgView11.invalidate()
            }
        }
        model.dataSrc12.observe(this) {
            if (this::ecgView12.isInitialized) {
                ecgView12.setDataSrc(it)
                ecgView12.invalidate()
            }
        }

        model.er3.observe(this) {
            device_sn.text = "SN：${it.sn}"

            about.isEnabled = true
            about.setTextColor(resources.getColor(R.color.colorPrimary))
        }

        model.connect.observe(this) {
            if (it) {
                ble_state.setImageResource(R.mipmap.bluetooth_ok)
                ecgViewVisible(true)
                battery.visibility = View.VISIBLE
                battery_left_duration.visibility = View.VISIBLE

                dialog?.dismiss()
                Toast.makeText(activity, "配对成功", Toast.LENGTH_SHORT).show()
//                bleInterface.runRtTask()
            } else {
                ble_state.setImageResource(R.mipmap.bluetooth_error)
                ecgViewVisible(false)
                battery.visibility = View.INVISIBLE
                battery_left_duration.visibility = View.INVISIBLE
                stopWave()
            }
        }

        model.duration.observe(this) {
            if (it == 0) {
                measure_duration.text = "?"
            } else {
                val day = it / 60 / 60 / 24
                val hour = it / 60 / 60 % 24
                val minute = it / 60 % 60

                val start = System.currentTimeMillis() - it * 1000
                if (day != 0) {
                    measure_duration.text = "$day 天 $hour 小时 $minute 分钟"
                } else {
                    measure_duration.text = "$hour 小时 $minute 分钟"
                }
            }
        }

        model.battery.observe(this) {
            battery.setImageLevel(it)
        }

        model.hr.observe(this) {
            if (it == 0) {
                hr.text = "?"
            } else {
                hr.text = it.toString()
            }
        }

        model.temp.observe(this) {
            temp.text = "TEMP: $it dec"
        }

        model.spo2.observe(this) {
            spo2.text = "SpO2: $it %"
        }
    }


    /**
     * observe LiveDataBus
     * receive from KcaBleInterface
     * 考虑直接从interface来控制，不需要所有的都传递
     */
    private fun addLiveEventObserver() {
        LiveEventBus.get(EventMsgConst.EventDeviceChoosen)
                .observe(this) {
                    connect(it as Bluetooth)
                    dialog = ProgressDialog(activity)
                    dialog?.setMessage("正在配对")
                    dialog?.setCancelable(false)
                    dialog?.show()
                }

        LiveEventBus.get(EventMsgConst.EventEr3GetConfig)
            .observe(this) {
                val msg = when (it as Int) {
                    0 -> "监护模式"
                    1 -> "手术模式"
                    2 -> "ST模式"
                    else -> ""
                }

                Toast.makeText(this.activity, msg, Toast.LENGTH_SHORT).show()
                planets_spinner.setSelection(it, true)
            }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun connect(b: Bluetooth) {
        this@Er3Fragment.context?.let { bleInterface.connect(it, b.device) }
    }

    companion object {

        @JvmStatic
        fun newInstance() = Er3Fragment()
    }
}