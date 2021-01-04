package com.lepu.lepuble.utils

import java.text.SimpleDateFormat
import java.util.*

public fun makeTimeStr(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd,HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}