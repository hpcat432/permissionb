package com.hpcat.permissionb.manufacture

import android.content.Context
import android.content.Intent

interface IManufacturer {

    fun getPermissionSettingsIntent(context: Context): Intent

}