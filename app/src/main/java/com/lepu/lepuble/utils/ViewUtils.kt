package com.lepu.lepuble.utils

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable


fun createDrawable(color: Int, corner: Float): Drawable {

    return createDrawable(
        color = color,
        topLeft = corner, topRight = corner,
        bottomRight = corner, bottomLeft = corner
    )

}

fun createDrawable(
    color: Int,
    topLeft: Float, topRight: Float,
    bottomRight: Float, bottomLeft: Float
): Drawable {


    var gradientDrawable = GradientDrawable()
    gradientDrawable.setColor(color)
    gradientDrawable.cornerRadii = floatArrayOf(
        topLeft, topLeft,
        topRight, topRight,
        bottomRight,bottomRight,
        bottomLeft,bottomLeft)

    return gradientDrawable

}

fun createCircleDrawable(color: Int, corner: Float): Drawable {

    var drawable = GradientDrawable()
    drawable.shape = GradientDrawable.OVAL
    drawable.setColor(color)

    return drawable

}

fun  createStrokeCircleDrawable(outColor: Int, interColor: Int,
                            strpkeWidth: Int): Drawable {
   var drawable =  GradientDrawable()
   drawable.shape = GradientDrawable.OVAL
   drawable.setColor(interColor)
   drawable.setStroke(strpkeWidth,outColor)
   return drawable
}





