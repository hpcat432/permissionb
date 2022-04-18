package com.hpcat.permissionb.utils

import android.content.Context

fun getStatusBarHeight(context: Context): Int {
    var result = 0
    val resourceId: Int = context.resources.getIdentifier("status_bar_height",
        "dimen", "android")
    if (resourceId > 0) {
        result = context.resources.getDimensionPixelSize(resourceId)
    }
    return result
}

fun dipToPx(context: Context, dipValue: Float): Float {
    val scale = context.resources.displayMetrics.density
    return dipValue * scale + 0.5f
}