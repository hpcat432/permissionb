package com.hpcat.permissionb.manufacture

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build

class VivoManufacturer : IManufacturer {
    override fun getPermissionSettingsIntent(context: Context): Intent {
        val intent = Intent().apply {
            putExtra("packageName", context.packageName)
            component = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                ComponentName("com.vivo.settings", "com.vivo.VivoSubSettings")
            } else {
                ComponentName("com.iqoo.secure",
                    "com.iqoo.secure.safeguard.SoftPermissionDetailActivity")
            }
        }
        return intent
    }
}