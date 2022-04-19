package com.hpcat.permissionb.manufacture

import android.content.ComponentName
import android.content.Context
import android.content.Intent

class HuaweiManufacturer : IManufacturer {
    override fun getPermissionSettingsIntent(context: Context): Intent {
        val intent = Intent()
        intent.putExtra("packageName", context.packageName)
        val componentName = ComponentName("com.android.packageinstaller",
            "com.android.packageinstaller.permission.ui.ManagePermissionsActivity")
        intent.component = componentName
        return intent
    }
}