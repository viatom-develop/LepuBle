package com.lepu.lepuble.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.blankj.utilcode.util.LogUtils
import com.lepu.lepuble.BuildConfig
import com.lepu.lepuble.R
import com.lepu.lepuble.objs.Bluetooth
import com.lepu.lepuble.vals.curModel

class PermissionActivity : AppCompatActivity() {

    private val permissionRequestCode = 521

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        checkModel()

        checkVersion()

//        requestLocation()
    }

    private fun checkModel() {
        when(BuildConfig.FLAVOR) {
            "er1" -> curModel = Bluetooth.MODEL_ER1
            "er2" -> curModel = Bluetooth.MODEL_ER2
            "er3" -> curModel = Bluetooth.MODEL_ER3
            "oxy" -> curModel = Bluetooth.MODEL_CHECKO2
            "airbp" -> curModel = Bluetooth.MODEL_AIRBP
            "bp2" -> curModel = Bluetooth.MODEL_BP2
            "kca" -> curModel = Bluetooth.MODEL_KCA
            "s1" -> curModel = Bluetooth.MODEL_S1
            "p1" -> curModel = Bluetooth.MODEL_P1
            "am300b" -> curModel = Bluetooth.MODEL_300B
            "aed" -> curModel = Bluetooth.MODEL_AED
            "monitor" -> curModel = Bluetooth.MODEL_MONITOR

            else -> curModel = Bluetooth.MODEL_ER1
        }
    }

    private fun checkVersion() {
        val version = android.os.Build.VERSION.SDK_INT
        if (version < android.os.Build.VERSION_CODES.N) {
            MaterialDialog(this).show {
                title(text = "NOTICE")
                message(text = "Your phone is not supported, please update to Android 7.0!")
                positiveButton(text = "Confirm") {
                    finish()
                }
            }
        } else {
            requestLocation()
            requestPermission()
//            checkBt()
        }
    }

    private fun requestLocation() {
        /**
         * 检查是否开启location
         */
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        val enable = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        LogUtils.d("location enable: $enable")

        if (!enable) {
            Toast.makeText(this, "请打开手机定位", Toast.LENGTH_SHORT).show()
        } else {
            requestPermission()
        }
    }

    private fun requestPermission() {
        val ps : Array<String> = arrayOf(
//            Manifest.permission.ACCESS_WIFI_STATE,
//            Manifest.permission.CHANGE_WIFI_STATE,
//            Manifest.permission.ACCESS_NETWORK_STATE,
//            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.CAMERA,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        for (p  in ps) {
            if (!checkP(p)) {
                ActivityCompat.requestPermissions(this, ps, permissionRequestCode)
                return
            }
        }

        permissionFinished()
    }

    private fun checkP(p: String) : Boolean {
        return ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
    }

    private fun permissionFinished() {
        checkBt()
    }

    private fun checkBt() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(this, "不支持蓝牙", Toast.LENGTH_SHORT).show()
            return
        }

        if (adapter.isEnabled) {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            this.finish()
        } else {
            if (adapter.enable()) {
                val i = Intent(this, MainActivity::class.java)
                startActivity(i)
                this.finish()
            } else {
                Toast.makeText(this, "蓝牙打开失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requestLocation()
    }
}