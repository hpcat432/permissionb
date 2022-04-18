package com.hpcat.permissionb.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import androidx.core.app.ActivityCompat
import com.hpcat.permissionb.R

val sPermissionNameMap = mutableMapOf<String, Int>().apply {
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

fun getPermissionInfoString(context: Context, vararg permissions: String): String {
    val resIds = mutableListOf<Int>()
    val builder = StringBuilder()
    permissions.forEach {
        if (sPermissionNameMap.containsKey(it)) {
            val id = sPermissionNameMap[it]
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

fun shouldShowRequestPermissionRationale(activity: Activity, vararg permissions: String): Boolean {
    permissions.forEach {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, it)) {
            return true
        }
    }
    return false
}