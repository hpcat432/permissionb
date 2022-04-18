package com.hpcat.permissionb.request

import android.os.Build
import androidx.fragment.app.FragmentActivity
import com.hpcat.permissionb.fragment.RequestFragment

class VersionCheckRequest(
    override var activity: FragmentActivity,
    override var callbackList: MutableList<RequestCallback>,
    override var next: Request?,
    override var permission: String,
    private val version: Int
) : AbsActualRequest() {
    override fun request(requestList: MutableList<AbsActualRequest>) {
        // this permission is automatically granted below {version}.
        if (Build.VERSION.SDK_INT >= version) {
            requestList.add(this)
        }
        if (next == null) {
            RequestFragment.requestWithFragment(activity, requestList, callbackList, needHideNeverAskDialog)
        }
        next?.request(requestList)
    }
}