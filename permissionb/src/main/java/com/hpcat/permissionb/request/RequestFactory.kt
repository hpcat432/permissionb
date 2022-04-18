package com.hpcat.permissionb.request

import android.Manifest
import android.os.Build
import androidx.fragment.app.FragmentActivity

object RequestFactory {

    private val versionCheckPermissionMap = mapOf(
        Manifest.permission.WRITE_SETTINGS to Build.VERSION_CODES.M,
        Manifest.permission.SYSTEM_ALERT_WINDOW to Build.VERSION_CODES.M)

    fun buildRequest(activity: FragmentActivity,
                     permission: String,
                     needRationaleDialog: Boolean,
                     needHideNeverAskDialog: Boolean,
                     showTopHint: Boolean,
                     next: Request?,
                     callbackList: MutableList<RequestCallback>): Request {
        var request = getOriginRequest(activity, permission, next, needHideNeverAskDialog, callbackList)
        if (needRationaleDialog) {
            request = RationalDialogWrapRequest(request, callbackList, activity)
        }
        if (showTopHint) {
            request = TopHintWrapRequest(request, callbackList, activity)
        }
        return request
    }

    private fun getOriginRequest(activity: FragmentActivity,
                                 permission: String,
                                 next: Request?,
                                 needHideNeverAskDialog: Boolean,
                                 callbackList: MutableList<RequestCallback>): Request {
        var request: AbsActualRequest? = null
        if (permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION) {
            request = BackgroundLocationRequest(activity, callbackList, next, permission)
        } else if (versionCheckPermissionMap.containsKey(permission)) {
            val version = versionCheckPermissionMap[permission]
            version?.let {
                request = VersionCheckRequest(activity, callbackList, next, permission , it)
            }
        }
        if (request == null) {
            request = NormalRequest(activity, permission, callbackList, next)
        }
        request?.needHideNeverAskDialog = needHideNeverAskDialog
        return request!!
    }

}