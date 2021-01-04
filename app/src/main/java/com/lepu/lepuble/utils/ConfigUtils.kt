package com.lepu.lepuble.utils

import android.content.Context

const val KEY_LEAD_INFO = "lead_info"

const val KEY_HOST_IP = "host_ip"
const val KEY_HOST_PORT = "host_port"

//ER1
const val KEY_ER1_BLE_NAME = "er1_ble_name"

//O2Max
const val KEY_OXY_BLE_NAME = "oxy_ble_name"

// 康康血压计
const val KEY_KCA_BLE_NAME = "kca_ble_name"

// if lock scrren
const val KEY_LOCK_SCREEN = "lock_scrren"

// skip agreement
const val KeySkipAgreement = "skip_agreement"

data class HostConfig(val ip: String?, val port: Int?)

fun saveConfig(context: Context, key: String, value: String) {
    PreferenceUtils.savePreferences(context, key, value)
}

fun readLockScreen(context: Context) : Boolean {

    return PreferenceUtils.readBoolPreferences(context, KEY_LOCK_SCREEN)
}

fun saveLockScreen(context: Context, lock: Boolean) {
    PreferenceUtils.savePreferences(context, KEY_LOCK_SCREEN, lock)
}

fun readLeadInfo(context: Context) : Int {
    val info = PreferenceUtils.readIntPreferences(context, KEY_LEAD_INFO)

    return if (info == 0) {
        0x02
    } else {
        info
    }
}

fun saveLeadInfo(context: Context, info: Int) {
    PreferenceUtils.savePreferences(context, KEY_LEAD_INFO, info)
}

fun clearLeadInfo(context: Context) {
    saveLeadInfo(context, 0x02)
}

fun saveHostConfig(context: Context, ip: String, port: Int) {
    PreferenceUtils.savePreferences(context, KEY_HOST_IP, ip)
    PreferenceUtils.savePreferences(context, KEY_HOST_PORT, port)
}

fun readHostConfig(context: Context) : HostConfig {

    val ip = PreferenceUtils.readStrPreferences(context, KEY_HOST_IP)
    val port = PreferenceUtils.readIntPreferences(context, KEY_HOST_PORT)

    return HostConfig(ip, port)
}

fun saveEr1Config(context: Context, name: String) {
    PreferenceUtils.savePreferences(context, KEY_ER1_BLE_NAME, name)
}

fun readEr1Config(context: Context) : String? {
    return PreferenceUtils.readStrPreferences(context, KEY_ER1_BLE_NAME)
}

fun clearEr1Config(context: Context) {
    PreferenceUtils.removeStrPreferences(context, KEY_ER1_BLE_NAME)
}


fun saveOxyConfig(context: Context, name: String) {
    PreferenceUtils.savePreferences(context, KEY_OXY_BLE_NAME, name)
}

fun readOxyConfig(context: Context) : String? {
    return PreferenceUtils.readStrPreferences(context, KEY_OXY_BLE_NAME)
}

fun clearOxyConfig(context: Context) {
    PreferenceUtils.removeStrPreferences(context, KEY_OXY_BLE_NAME)
}


fun saveKcaConfig(context: Context, name: String) {
    PreferenceUtils.savePreferences(context, KEY_KCA_BLE_NAME, name)
}

fun readKcaConfig(context: Context) : String? {
    return PreferenceUtils.readStrPreferences(context, KEY_KCA_BLE_NAME)
}

fun clearKcaConfig(context: Context) {
    PreferenceUtils.removeStrPreferences(context, KEY_KCA_BLE_NAME)
}

fun readAgreementConfig(context: Context): Boolean {
    return PreferenceUtils.readBoolPreferences(context, KeySkipAgreement)
}

fun saveAgreementConfig(context: Context, b: Boolean) {
    PreferenceUtils.savePreferences(context, KeySkipAgreement, b)
}

fun clearHostConfig(context: Context) {
    PreferenceUtils.removeStrPreferences(context, KEY_HOST_IP)
    PreferenceUtils.removeStrPreferences(context, KEY_HOST_PORT)
}

fun clearConfig(context: Context) {
    PreferenceUtils.removeAllPreferences(context)
}