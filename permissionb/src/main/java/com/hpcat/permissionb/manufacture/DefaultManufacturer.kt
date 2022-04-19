package com.hpcat.permissionb.manufacture

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

class DefaultManufacturer : IManufacturer {
    override fun getPermissionSettingsIntent(context: Context): Intent {
        val pkgURI = Uri.parse("package:" + context.packageName)
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, pkgURI)
    }
}