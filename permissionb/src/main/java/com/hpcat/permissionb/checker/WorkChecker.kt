package com.hpcat.permissionb.checker

import android.content.Context

class WorkChecker: IPermissionChecker {
    override fun check(context: Context, permission: String): Boolean {
        return true
    }
}