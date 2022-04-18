package com.hpcat.permissionb.checker

import android.content.Context

interface IPermissionChecker {

    fun check(context: Context, permission: String): Boolean

}