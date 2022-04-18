package com.hpcat.permissionb.request

import android.app.Activity
import androidx.fragment.app.FragmentActivity

/**
 * 实际请求（非装饰器）
 */
abstract class AbsActualRequest: Request {

    var needHideNeverAskDialog = false

    abstract var activity: FragmentActivity
    abstract var callbackList: MutableList<RequestCallback>
    abstract var next: Request?

}