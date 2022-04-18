package com.hpcat.permissionb.request

import android.app.Activity
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.hpcat.permissionb.PermissionB
import com.hpcat.permissionb.R
import com.hpcat.permissionb.utils.dipToPx
import com.hpcat.permissionb.utils.getStatusBarHeight
import com.hpcat.permissionb.view.ViewDialog

/**
 * 顶部蒙层提示的装饰器，如需申请权限同时展示蒙层提示，请用它包裹Request
 * 当前一次权限申请只支持一次蒙层展示
 */
class TopHintWrapRequest(
    override var originRequest: Request,
    override var callbackList: MutableList<RequestCallback>,
    val activity: Activity
) : AbsWrapRequest() {
    override fun request(requestList: MutableList<AbsActualRequest>) {
        val dialog = getTopDialog(originRequest.permission)
        val callback = object : RequestCallback {
            override fun onResult(
                allGrated: Boolean,
                grantList: List<String>,
                denyList: List<Deny>
            ) {
                dialog?.dismiss()
            }
        }
        dialog?.safeShow()
        callbackList.add(callback)
        originRequest.request(requestList)
    }

    private fun getTopDialog(permission: String): ViewDialog? {
        val titleText = getTitleText(permission)
        val descText = getDescText(permission)
        if (TextUtils.isEmpty(titleText) || TextUtils.isEmpty(descText)) {
            return null
        }
        val dialogContent = LayoutInflater.from(activity)
            .inflate(R.layout.main_top_permission_layout, null, false)
        dialogContent.findViewById<ViewGroup>(R.id.permission_content_layout).apply {
            (layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin =
                (getStatusBarHeight(activity) + dipToPx(activity, 16F)).toInt()
        }
        dialogContent.findViewById<TextView>(R.id.permission_request_title).text = titleText
        dialogContent.findViewById<TextView>(R.id.permission_request_desc).text = descText
        return ViewDialog(activity.findViewById<View>(android.R.id.content).rootView as ViewGroup,
            activity, dialogContent, Gravity.TOP)
            .apply {
                setOutsideColor(ContextCompat.getColor(activity, R.color.permission_transparent))
            }
    }

    private fun getTitleText(permission: String): String? {
        return PermissionB.sTopHintTextMap?.get(permission)?.first
    }

    private fun getDescText(permission: String): String? {
        return PermissionB.sTopHintTextMap?.get(permission)?.second
    }
}