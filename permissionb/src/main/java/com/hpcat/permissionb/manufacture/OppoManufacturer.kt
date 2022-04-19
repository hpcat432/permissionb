package com.hpcat.permissionb.manufacture

import android.content.ComponentName
import android.content.Context
import android.content.Intent

class OppoManufacturer : IManufacturer {
    override fun getPermissionSettingsIntent(context: Context): Intent {
        val intent = Intent()
        intent.putExtra("packageName", context.packageName)
        val componentName = ComponentName("com.coloros.securitypermission",
            "com.coloros.securitypermission.permission.PermissionGroupsActivity")
        intent.component = componentName
        return intent
    }
}