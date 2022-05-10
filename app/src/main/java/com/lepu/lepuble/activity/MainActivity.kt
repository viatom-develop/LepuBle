package com.lepu.lepuble.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.lepuble.BuildConfig
import com.lepu.lepuble.R
import com.lepu.lepuble.ble.BleLogs
import com.lepu.lepuble.fragments.*
import com.lepu.lepuble.objs.BleAdapter
import com.lepu.lepuble.objs.BleLogItem
import com.lepu.lepuble.objs.Bluetooth
import com.lepu.lepuble.utils.HexString
import com.lepu.lepuble.vals.EventMsgConst
import com.lepu.lepuble.vals.EventMsgConst.EventMsgSendCmd
import com.lepu.lepuble.vals.curModel
import com.lepu.lepuble.viewmodel.MainViewModel
import com.lepu.lepuble.views.LogAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.toolbar_title
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private val mainModel : MainViewModel by viewModels()

    private var logItems: ArrayList<BleLogItem> = ArrayList()
    var totalSend = 0
    var totalReceive = 0
    var pkgSend = 0
    var pkgRec = 0

    lateinit var logAdapter: LogAdapter
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

          val behavior = BottomSheetBehavior.from(btm_sheet)
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState) {
                    /** The bottom sheet is dragging.  */
                    BottomSheetBehavior.STATE_DRAGGING -> {}

                    /** The bottom sheet is settling.  */
                    BottomSheetBehavior.STATE_SETTLING -> {}

                    /** The bottom sheet is expanded.  */
                    BottomSheetBehavior.STATE_EXPANDED -> {}

                    /** The bottom sheet is collapsed.  */
                    BottomSheetBehavior.STATE_COLLAPSED -> {}

                    /** The bottom sheet is hidden.  */
                    BottomSheetBehavior.STATE_HIDDEN -> {}
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

        })

        clear.setOnClickListener {
            totalSend = 0
            totalReceive = 0
            logItems.clear()

            logAdapter.notifyDataSetChanged()
            send.text =  "SEND: $totalSend"
            receive.text = "RECEIVE: $totalReceive"
        }

        action_send.setOnClickListener {
            val cmdStr: String = send_cmd.text.toString().trim()
            if ((cmdStr.isEmpty()) or (cmdStr.length % 2 != 0)) {
                Toast.makeText(this, "命令错误", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            LiveEventBus.get(EventMsgSendCmd).post(cmdStr)
        }

        logs.layoutManager = LinearLayoutManager(this)
        logAdapter = LogAdapter(this, logItems)
        logs.adapter = logAdapter
//        logAdapter = LogAdapter(BleLogs.logs.toTypedArray())
//        logs.adapter = logAdapter
//        updateLogs()

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
            Bluetooth.MODEL_300B -> {
                fragment = Am300bFragment.newInstance()
            }
            Bluetooth.MODEL_AED -> {
                fragment = AedFragment.newInstance()
            }

            Bluetooth.MODEL_MONITOR -> {
                fragment = MonitorFragment.newInstance()
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
//        mainModel.totalSend.observe(this, {
//            send.text = "SEND: ${it}"
//        })
//        mainModel.totalReceive.observe(this, {
//            receive.text = "RECEIVE: ${it}"
//        })
//        mainModel.logs.observe(this, {
////            logAdapter = LogAdapter(it)
////            logs.adapter = logAdapter
//            logItems.add(it[-1])
//            logAdapter.notifyItemInserted(-1)
//        })
    }


    @SuppressLint("SetTextI18n")
    private fun observeLiveDataObserve() {

        LiveEventBus.get(EventMsgConst.EventBleLog).observe(this) {
            val item = it as BleLogItem
            logItems.add(item)
            logAdapter.notifyItemInserted(logItems.size - 1)

            if (item.type == BleLogItem.SEND) {
                totalSend += item.content.size
                send.text = "SEND: $totalSend"
            }
            if (item.type == BleLogItem.RECEIVE) {
                totalReceive += item.content.size
                receive.text = "RECEIVE: $totalReceive"
            }
        }

        LiveEventBus.get(EventMsgConst.EventBlePkg).observe(this) {
            (it as Int).apply {
                if (it == 1) {
                    pkgSend++
                }
                if (it == 2) {
                    pkgRec++
                }
                pkgs.text = "PKG: ${pkgSend}/${pkgRec}"
            }
        }
    }
}