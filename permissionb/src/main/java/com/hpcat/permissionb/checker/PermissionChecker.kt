package com.hpcat.permissionb.checker

import android.content.Context
import android.os.Build

object PermissionChecker {

    const val GRANTED = 1
    const val GRANTED_BUT_NOT_WORK = 2
    const val DENY = 3

    private val standardChecker = StandardChecker()
    private val workChecker = StandardChecker()

    fun check(context: Context, permission: String): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return if (workChecker.check(context, permission)) GRANTED else DENY
        }
        val isPermissionGranted = standardChecker.check(context, permission)
        val isPermissionWork = workChecker.check(context, permission)
        return if (isPermissionGranted && isPermissionWork) {
            GRANTED
        } else if (isPermissionGranted) {
            GRANTED_BUT_NOT_WORK
        } else {
            DENY
        }
    }

}