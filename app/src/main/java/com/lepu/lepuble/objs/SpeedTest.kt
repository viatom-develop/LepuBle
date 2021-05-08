package com.lepu.lepuble.objs


object SpeedTest {

    var totalSize: Int = 0
    var totalTime: Long = 0
    var list = mutableListOf<D>()
    var speed: Float = 0.0f


    fun add(size: Int): Float {
        val t = System.currentTimeMillis()
        list.add(D(t, size))

        var curSize = 0
        var start = 0L
        var end = 0L

        list.removeIf {
            t - it.time > 10000
        }

        for (d in list) {
            curSize += d.size
            if (start == 0L) {
                start = d.time
            } else if (start > d.time) {
                start = d.time
            }
            if (end == 0L) {
                end = d.time
            } else if (end < d.time) {
                end = d.time
            }
        }

        if (end - start != 0L) {
            speed = curSize.toFloat() / (end - start)
        }

        return speed
    }



    fun init() {
        totalSize = 0
        totalTime = 0
        list.clear()
        speed = 0.0f
    }


    class D(var time: Long, var size: Int)
}