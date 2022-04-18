package com.hpcat.permissionb.request

import android.Manifest
import android.os.Build
import androidx.fragment.app.FragmentActivity
import com.hpcat.permissionb.fragment.RequestFragment

/**
 * 后台位置权限比较特殊，需要先挂起，在前台位置权限结果返回后再发起
 */
class BackgroundLocationRequest(
    override var activity: FragmentActivity,
    override var callbackList: MutableList<RequestCallback>,
    override var next: Request?,
    override var permission: String
) : AbsActualRequest() {
    override fun request(requestList: MutableList<AbsActualRequest>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (next == null) {
                RequestFragment.requestWithFragment(activity, requestList, callbackList, needHideNeverAskDialog)
            }
            next?.request(requestList)
        } else {
            callbackList.add(object : RequestCallback {
                override fun onResult(
                    allGrated: Boolean,
                    grantList: List<String>,
                    denyList: List<Deny>
                ) {
                    if (grantList.contains(Manifest.permission.ACCESS_FINE_LOCATION)
                        || grantList.contains(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        val singleCallback = mutableListOf(callbackList[0])
                        val reqList = mutableListOf<AbsActualRequest>().apply {
                            add(NormalRequest(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION, singleCallback, null))
                        }
                        RequestFragment.requestWithFragment(activity, reqList, singleCallback, needHideNeverAskDialog)
                    }
                }
            })
            if (next == null) {
                RequestFragment.requestWithFragment(activity, requestList, callbackList, needHideNeverAskDialog)
            }
            next?.request(requestList)

        }
    }
}