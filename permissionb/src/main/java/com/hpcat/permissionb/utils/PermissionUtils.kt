package com.hpcat.permissionb.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import com.hpcat.permissionb.R
import com.hpcat.permissionb.manufacture.*

val PERMISSION_MAP = hashMapOf<String, Int>().apply {
    put(Manifest.permission.READ_CALENDAR, R.string.permissions_calendar)
    put(Manifest.permission.WRITE_CALENDAR, R.string.permissions_calendar)
    put(Manifest.permission.CAMERA, R.string.permissions_camera)
    put(Manifest.permission.READ_CONTACTS, R.string.permissions_contact)
    put(Manifest.permission.WRITE_CONTACTS, R.string.permissions_contact)
    put(Manifest.permission.ACCESS_COARSE_LOCATION, R.string.permissions_location)
    put(Manifest.permission.ACCESS_FINE_LOCATION, R.string.permissions_location)
    put(Manifest.permission.ACCESS_BACKGROUND_LOCATION, R.string.permissions_location)
    put(Manifest.permission.RECORD_AUDIO, R.string.permissions_mic)
    put(Manifest.permission.READ_PHONE_STATE, R.string.permissions_read_phone_state)
    put(Manifest.permission.CALL_PHONE, R.string.permissions_call)
    put(Manifest.permission.READ_CALL_LOG, R.string.permissions_call_log)
    put(Manifest.permission.WRITE_CALL_LOG, R.string.permissions_call_log)
    put(Manifest.permission.BODY_SENSORS, R.string.permissions_sensors)
    put(Manifest.permission.SEND_SMS, R.string.permissions_sms)
    put(Manifest.permission.READ_SMS, R.string.permissions_sms)
    put(Manifest.permission.RECEIVE_SMS, R.string.permissions_sms)
    put(Manifest.permission.RECEIVE_WAP_PUSH, R.string.permissions_sms)
    put(Manifest.permission.RECEIVE_MMS, R.string.permissions_sms)
    put(Manifest.permission.WRITE_CALL_LOG, R.string.permissions_sms)
    put(Manifest.permission.READ_EXTERNAL_STORAGE, R.string.permissions_storage)
    put(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permissions_storage)
}

val MANUFACTURER_MAP = hashMapOf<String, IManufacturer>().apply {
    put("VIVO", VivoManufacturer())
    put("MEIZU", MeizuManufacturer())
    put("HUAWEI", HuaweiManufacturer())
    put("XIAOMI", XiaomiManufacturer())
    put("OPPO", OppoManufacturer())
}

fun getPermissionInfoString(context: Context, vararg permissions: String): String {
    val resIds = mutableListOf<Int>()
    val builder = StringBuilder()
    permissions.forEach {
        if (PERMISSION_MAP.containsKey(it)) {
            val id = PERMISSION_MAP[it]
            if (!resIds.contains(id)) {
                resIds.add(id?: 0)
            }
        }
        resIds.forEach { resId ->
            if (builder.isNotEmpty()) {
                builder.append("，")
            }
            builder.append(context.getString(resId))
        }
    }
    return builder.toString()
}

fun shouldShowRationale(activity: Activity?, vararg permissions: String): Boolean {
    permissions.forEach {
        activity?.let { activity ->
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, it)) {
                return true
            }
        }
    }
    return false
}

fun canGotoWriteSettingsPage(context: Context): Boolean {
    return isIntentAvailable(context, getWriteSettingsIntent(context))
}

fun getWriteSettingsIntent(context: Context): Intent {
    val pkgURI = Uri.parse("package:" + context.packageName)
    return Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, pkgURI)
}

fun canGotoAppSystemSettingPage(context: Context): Boolean {
    return isIntentAvailable(context, getAppSettingsIntent(context))
}

fun canGotoAppInfoPage(context: Context): Boolean {
    return isIntentAvailable(context, getAppInfoIntent(context))
}

fun getAppInfoIntent(context: Context): Intent {
    val pkgUri = Uri.parse("package:" + context.packageName)
    return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, pkgUri)
}

fun getAppSettingsIntent(context: Context): Intent {
    val manufacturer = Build.MANUFACTURER
    return if (TextUtils.isEmpty(manufacturer) || !MANUFACTURER_MAP.containsKey(manufacturer)) {
        DefaultManufacturer().getPermissionSettingsIntent(context)
    } else {
        MANUFACTURER_MAP[manufacturer]?.getPermissionSettingsIntent(context)
            ?: DefaultManufacturer().getPermissionSettingsIntent(context)
    }
}

fun isIntentAvailable(context: Context, intent: Intent): Boolean {
    val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
    if (resolveInfo?.activityInfo == null
        || resolveInfo.activityInfo.name.toLowerCase().contains("resolver")) {
        return false
    }
    val permission = resolveInfo.activityInfo.permission
    if (!TextUtils.isEmpty(permission)) {
        val result = context.checkCallingOrSelfPermission(permission)
        return result == PackageManager.PERMISSION_GRANTED
    }
    return true
}