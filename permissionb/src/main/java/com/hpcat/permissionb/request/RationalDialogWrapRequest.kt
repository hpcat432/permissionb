package com.hpcat.permissionb.request

import android.app.AlertDialog
import android.content.DialogInterface
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import com.hpcat.permissionb.PermissionB
import com.hpcat.permissionb.R
import com.hpcat.permissionb.fragment.RequestFragment
import com.hpcat.permissionb.utils.getPermissionInfoString

/**
 * 装饰器Request，如果需要先弹窗提示再请求权限，请用它包裹原Request
 */
class RationalDialogWrapRequest(
    override var originRequest: Request,
    override var callbackList: MutableList<RequestCallback>,
    val activity: FragmentActivity
) : AbsWrapRequest() {

    override fun request(requestList: MutableList<AbsActualRequest>) {
        // 检查是否弹过相同弹窗，例如ACCESS_COARSE_LOCATION、ACCESS_FINE_LOCATION对应的都是"定位"权限
        val isDialogShown = checkSameDialog(requestList, originRequest.permission)
        if (isDialogShown) {
            originRequest.request(requestList)
            return
        }
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(activity.getString(R.string.permission_request)).setMessage(getMessage())
            .setPositiveButton(R.string.confirm
            ) { dialog, which -> originRequest.request(requestList) }.setNegativeButton(R.string.cancel
            ) { dialog, which ->
                // 找到最里面的ActualRequest，发起next节点的请求
                var innerActualRequest = originRequest
                while (innerActualRequest is AbsWrapRequest) {
                    innerActualRequest = innerActualRequest.originRequest
                }
                if (innerActualRequest is AbsActualRequest) {
                    // 跳过当前权限，进入下一个Request，如果下一个为空，立即发起请求
                    if (innerActualRequest.next == null) {
                        RequestFragment.requestWithFragment(activity, requestList, callbackList)
                    } else {
                        innerActualRequest.next?.request(requestList)
                    }
                    // 先把这个请求以失败结果回调回去，流程继续
                    callbackList[0].onResult(
                        false,
                        listOf(),
                        listOf(Deny(originRequest.permission, false))
                    )
                }
            }
    }

    private fun getMessage(): String {
        val message = PermissionB.sRationalDialogTextMap?.get(originRequest.permission)
        return if (!TextUtils.isEmpty(message)) {
            message?: ""
        } else {
            activity.getString(R.string.permission_dialog_info_message, getPermissionInfoString(activity, originRequest.permission))
        }
    }

    private fun checkSameDialog(requestList: MutableList<AbsActualRequest>, permission: String): Boolean {
        return requestList.map {
            getPermissionInfoString(activity, it.permission)
        }.contains(getPermissionInfoString(activity, permission))
    }


}