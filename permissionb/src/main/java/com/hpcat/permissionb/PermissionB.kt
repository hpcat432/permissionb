package com.hpcat.permissionb

import android.os.Build
import androidx.fragment.app.FragmentActivity
import com.hpcat.permissionb.checker.PermissionChecker
import com.hpcat.permissionb.consts.IPermissionTrack
import com.hpcat.permissionb.request.Deny
import com.hpcat.permissionb.request.Request
import com.hpcat.permissionb.request.RequestCallback
import com.hpcat.permissionb.request.RequestFactory
import com.hpcat.permissionb.utils.ILogger
import com.hpcat.permissionb.utils.shouldShowRequestPermissionRationale
import org.json.JSONObject

class PermissionB(private val activity: FragmentActivity) {

    companion object {

        var sLogger: ILogger? = null

        // 定制前置弹窗文案
        var sRationalDialogTextMap: Map<String, String>? = null

        // 定制后置弹窗文案
        var sNeverAskDialogTextMap: Map<String, String>? = null

        // 定制蒙层提示文案
        var sTopHintTextMap: Map<String, Pair<String, String>>? = null

        // 控制默认展示前置弹窗
        var defaultShowRationaleDialog: Boolean? = null

        var defaultHideNeverAskDialog: Boolean? = null

    }

    private var requestCallback: RequestCallback? = null

    private val permissionList = mutableListOf<String>()

    // 多个环节都可能得出某个权限的请求结果，先得出结果的暂存在这里，最后统一返回
    private val resultList = mutableListOf<PermissionInnerResult>()

    private val grantedButNotWorkPermissionList = mutableListOf<String>()

    // 强制显示前置提示弹窗
    private var showRationaleDialog: Boolean? = null

    // 强制不展示后置弹窗
    private var hideNeverAskDialog: Boolean? = null
    private val hideNeverAskDialogPermissionList = mutableListOf<String>()

    // 展示顶部蒙层提示
    private var showTopHint: Boolean = false

    fun showRationaleDialog (needShowRationaleDialog: Boolean): PermissionB {
        this.showRationaleDialog = needShowRationaleDialog
        return this
    }

    fun hideNeverAskDialog(hideNeverAskDialog: Boolean): PermissionB {
        this.hideNeverAskDialog = hideNeverAskDialog
        return this
    }

    fun showTopHint(showTopHint: Boolean): PermissionB {
        this.showTopHint = showTopHint
        return this
    }

    fun callback(requestCallback: RequestCallback): PermissionB {
        this.requestCallback = requestCallback
        return this
    }

    fun request(vararg permissions: String) {
        permissionList.addAll(permissions)
        val permissionsNeedToRequest = mutableListOf(*permissions)

        permissions.forEach {
            val jsonObject = JSONObject().apply {
                put(IPermissionTrack.Key.PERMISSION_NAME, it)
            }
            sLogger?.log(IPermissionTrack.Action.EVENT_PERMISSION_REQUEST, jsonObject)
        }

        // 检查已有权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            val grantList = mutableListOf<String>()
            val denyList = mutableListOf<Deny>()
            var isAllGranted = true
            permissions.forEach {
                if (PermissionChecker.check(activity, it) == PermissionChecker.GRANTED) {
                    grantList.add(it)
                } else {
                    denyList.add(Deny(it, false))
                    isAllGranted = false
                }
            }
            requestCallback?.onResult(isAllGranted, grantList, denyList)
        } else {
            permissions.forEach {
                if (PermissionChecker.check(activity, it) == PermissionChecker.GRANTED) {
                    resultList.add(PermissionInnerResult(it, true, isNeverAsk = false))
                    permissionsNeedToRequest.remove(it)
                } else if (PermissionChecker.check(activity, it) == PermissionChecker.GRANTED_BUT_NOT_WORK) {
                    grantedButNotWorkPermissionList.add(it)
                }
            }
            if (resultList.size == permissions.size) {
                checkAndReturnResult()
            }
        }
        doRequestWithSystemAPI(permissionsNeedToRequest, shouldShowRequestPermissionRationale(activity, *permissions))
    }

    private fun doRequestWithSystemAPI(permissionsNeedToRequest: List<String>,
                                       shouldShowRequestPermissionRationale: Boolean) {
        var headRequest: Request? = null
        val callbackList = mutableListOf<RequestCallback>(object : RequestCallback {
            override fun onResult(
                allGrated: Boolean,
                grantList: List<String>,
                denyList: List<Deny>
            ) {
                mergeCallbackWithResultList(grantList, denyList)
            }
        })
        permissionsNeedToRequest.forEach {
            // 默认遵循系统建议弹出前置解释弹窗
            var needRationaleDialog = shouldShowRequestPermissionRationale
            // 全局默认设置是否出前置弹窗
            defaultShowRationaleDialog?.let { show ->
                needRationaleDialog = show
            }
            // 本次单独设置是否出前置弹窗
            showRationaleDialog?.let { forceShowRationaleDialog ->
                needRationaleDialog = forceShowRationaleDialog
            }

            val inHideNeverAskDialogList = hideNeverAskDialogPermissionList.contains(it)
            var needHideNeverAskDialog = inHideNeverAskDialogList || (defaultHideNeverAskDialog == true)
            hideNeverAskDialog?.let { hide ->
                needHideNeverAskDialog = hide
            }
            val shouldShowTopHint = this.showTopHint && !needRationaleDialog
            headRequest = RequestFactory.buildRequest(activity,
                it,
                needRationaleDialog,
                needHideNeverAskDialog,
                shouldShowTopHint,
                next = headRequest,
                callbackList = callbackList)
        }
        headRequest?.request(mutableListOf())
    }

    private fun mergeCallbackWithResultList(grantList: List<String>, denyList: List<Deny>) {
        resultList.addAll(grantList.map {
            PermissionInnerResult(it, isGrant = true, isNeverAsk = false)
        })
        resultList.addAll(denyList.map {
            PermissionInnerResult(it.permission, isGrant = false, it.isNeverAsk)
        })
        checkAndReturnResult()
    }

    private fun checkAndReturnResult() {
        // 结果还没有全部得出，先不返回
        if (resultList.size < permissionList.size) {
            return
        }
        var isAllGranted = true
        val grantList = mutableListOf<String>()
        val denyList = mutableListOf<Deny>()
        resultList.forEach {
            if (!it.isGrant) {
                isAllGranted = false
                denyList.add(Deny(it.name, it.isNeverAsk))
            } else {
                grantList.add(it.name)
            }
        }
        requestCallback?.onResult(isAllGranted, grantList, denyList)
        sendResultLog(grantList, denyList)
    }

    private fun sendResultLog(grantList: List<String>, denyList: List<Deny>) {
        if (sLogger == null) return
        grantList.forEach {
            val jsonObject = JSONObject().apply {
                put(IPermissionTrack.Key.PERMISSION_NAME, it)
                put(IPermissionTrack.Key.PERMISSION_REQUEST_RESULT, IPermissionTrack.Value.PERMISSION_ALLOW)
            }
            sLogger?.log(IPermissionTrack.Action.EVENT_PERMISSION_REQUEST_RESULT, jsonObject)
        }
        denyList.forEach {
            val jsonObject = JSONObject().apply {
                put(IPermissionTrack.Key.PERMISSION_NAME, it.permission)
                put(IPermissionTrack.Key.PERMISSION_REQUEST_RESULT, IPermissionTrack.Value.PERMISSION_DENY)
                put(IPermissionTrack.Key.PERMISSION_NEVER_ASK, if (it.isNeverAsk) "1" else "0")
            }
            sLogger?.log(IPermissionTrack.Action.EVENT_PERMISSION_REQUEST_RESULT, jsonObject)
        }
    }

    inner class PermissionInnerResult (var name: String, var isGrant: Boolean, var isNeverAsk: Boolean)

}