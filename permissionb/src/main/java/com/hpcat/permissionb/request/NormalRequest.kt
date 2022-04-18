package com.hpcat.permissionb.request

import androidx.fragment.app.FragmentActivity
import com.hpcat.permissionb.fragment.RequestFragment

class NormalRequest(
    override var activity: FragmentActivity,
    override var permission: String,
    override var callbackList: MutableList<RequestCallback>,
    override var next: Request?
) : AbsActualRequest() {

    override fun request(requestList: MutableList<AbsActualRequest>) {
        requestList.add(this)
        if (next == null) {
            RequestFragment.requestWithFragment(activity, requestList, callbackList, needHideNeverAskDialog)
        }
        next?.request(requestList)
    }
}