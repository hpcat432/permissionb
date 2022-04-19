package com.hpcat.permissionb.manufacture

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build

class MeizuManufacturer : IManufacturer {
    override fun getPermissionSettingsIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            DefaultManufacturer().getPermissionSettingsIntent(context)
        } else {
            val intent = Intent("com.meizu.safe.security.SHOW_APPSEC")
            intent.putExtra("packageName", context.packageName)
            intent.component = ComponentName("com.meizu.safe", "com.meizu.safe.security.AppSecActivity")
            intent
        }
    }
}