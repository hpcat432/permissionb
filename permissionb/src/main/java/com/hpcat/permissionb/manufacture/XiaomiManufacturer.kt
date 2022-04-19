package com.hpcat.permissionb.manufacture

import android.content.ComponentName
import android.content.Context
import android.content.Intent

class XiaomiManufacturer : IManufacturer {
    override fun getPermissionSettingsIntent(context: Context): Intent {
        val intent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
            component = ComponentName("com.miui.securitycenter",
                "com.miui.permcenter.permissions.PermissionsEditorActivity")
            putExtra("extra_pkgname", context.packageName)
        }
        return intent
    }
}