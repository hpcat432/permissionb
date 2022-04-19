package com.hpcat.permissionb.fragment

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.hpcat.permissionb.PermissionB
import com.hpcat.permissionb.R
import com.hpcat.permissionb.checker.PermissionChecker
import com.hpcat.permissionb.consts.IPermissionTrack
import com.hpcat.permissionb.request.AbsActualRequest
import com.hpcat.permissionb.request.Deny
import com.hpcat.permissionb.request.RequestCallback
import com.hpcat.permissionb.utils.*
import org.json.JSONObject
import java.lang.Exception

class RequestFragment : Fragment() {

    companion object {
        private const val FRAGMENT_TAG = "permission_fragment_tag"

        fun requestWithFragment(activity : FragmentActivity,
                                permissionList: List<AbsActualRequest>,
                                requestCallbackList: List<RequestCallback>,
                                needHideNeverAskDialog: Boolean = false) {
            if (permissionList.isEmpty()) {
                return
            }
            val fm = activity.supportFragmentManager
            var fragment = fm.findFragmentByTag(FRAGMENT_TAG) as? RequestFragment
            if (fragment == null) {
                fragment = RequestFragment()
                fm.beginTransaction().add(fragment, FRAGMENT_TAG).commitAllowingStateLoss()
                fm.executePendingTransactions()
            }
            fragment.needHideNeverAskDialog = needHideNeverAskDialog
            fragment.requestCallbackList = requestCallbackList
            fragment.permissionList = permissionList
            fragment.currentGrantList.clear()
            fragment.currentDenyList.clear()
            fragment.request(permissionList)
        }

    }



    private var requestCallbackList: List<RequestCallback>? = null
    private var permissionList: List<AbsActualRequest>? = null
    private var currentGrantList = mutableListOf<String>()
    private var currentDenyList = mutableListOf<Deny>()

    private var needHideNeverAskDialog = false

    private var singleLauncher: ActivityResultLauncher<String>? = null
    private var multiLauncher: ActivityResultLauncher<Array<String>>? = null
    private var settingsLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        singleLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val permissionName = permissionList?.get(0)?.permission?: ""
            if (granted) {
                requestCallbackList?.forEach {
                    it.onResult(true, mutableListOf(permissionName), mutableListOf())
                }
            } else {
                activity?.let {
                    currentGrantList = mutableListOf()
                    val isNeverAsk = !shouldShowRationale(it, permissionName)
                    currentDenyList = mutableListOf<Deny>().apply {
                        add(Deny(permissionName, isNeverAsk))
                    }
                    if (isNeverAsk && !needHideNeverAskDialog) {
                        showNeverAskDialog(listOf(permissionName))
                    } else {
                        requestCallbackList?.forEach { callback ->
                            callback.onResult(false, mutableListOf(), mutableListOf(Deny(permissionName, false)))
                        }
                    }
                }
            }
        }
        multiLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val neverAskList = mutableListOf<String>()
            it.keys.forEach { key ->
                if (it[key] == true) {
                    currentGrantList.add(key)
                } else {
                    currentDenyList.add(Deny(key, false))
                    if (!shouldShowRationale(activity)) {
                        neverAskList.add(key)
                    }
                }
            }
            if (neverAskList.isEmpty() || needHideNeverAskDialog) {
                callbackResult()
            } else {
                showNeverAskDialog(neverAskList)
            }
        }
        settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            activity?.let {
                val iterator = currentDenyList.iterator()
                while (iterator.hasNext()) {
                    val deny = iterator.next()
                    if (PermissionChecker.check(it, deny.permission) == PermissionChecker.GRANTED) {
                        iterator.remove()
                        currentGrantList.add(deny.permission)
                    }
                }
                callbackResult()
            }
        }
    }

    fun request(permissionList: List<AbsActualRequest>) {
        val filterList = permissionList.filter {
            !TextUtils.isEmpty(it.permission)
        }.map {
            it.permission
        }
        if (filterList.isEmpty()) {
            return
        }
        if (filterList.size == 1) {
            singleLauncher?.launch(filterList[0])
        } else if (filterList.size > 1) {
            multiLauncher?.launch(filterList.toTypedArray())
        }
    }

    private fun showNeverAskDialog(permissionList: List<String>) {
        val activity = activity?: return
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(getString(R.string.permission_request))
            .setMessage(getNeverAskMessage(permissionList))
            .setPositiveButton(getString(R.string.permission_settings), object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface?, which: Int) {
                    gotoSettings(permissionList)
                    permissionList.forEach {
                        val jsonObject = JSONObject().apply {
                            put(IPermissionTrack.Key.TYPE, it + "_request")
                            put(IPermissionTrack.Key.ACTION, IPermissionTrack.Value.ENTER_SETTINGS)
                        }
                        PermissionB.sLogger?.log(IPermissionTrack.Action.POPUPS_CLICK, jsonObject)
                    }
                }
            })
            .setNegativeButton(R.string.cancel, object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface?, which: Int) {
                    callbackResult()
                    permissionList.forEach {
                        val jsonObject = JSONObject().apply {
                            put(IPermissionTrack.Key.TYPE, it + "_request")
                            put(IPermissionTrack.Key.ACTION, IPermissionTrack.Value.CLOSE)
                        }
                        PermissionB.sLogger?.log(IPermissionTrack.Action.POPUPS_CLICK, jsonObject)
                    }
                }

            }).show()
        permissionList.forEach {
            val jsonObject = JSONObject().apply {
                val jsonObject = JSONObject().apply {
                    put(IPermissionTrack.Key.TYPE, it + "_request")
                }
                PermissionB.sLogger?.log(IPermissionTrack.Action.POPUPS, jsonObject)
            }
        }
    }

    private fun gotoSettings(permissions: List<String>) {
        if (permissions.size == 1 && permissions[0] == Manifest.permission.WRITE_SETTINGS) {
            gotoWriteSettings()
        } else {
            gotoAppSettings()
        }
    }

    private fun gotoWriteSettings() {
        try {
            activity?.let {
                if (canGotoWriteSettingsPage(it)) {
                    val intent = getWriteSettingsIntent(it)
                    settingsLauncher?.launch(intent)
                } else {
                    callbackResult()
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun gotoAppSettings() {
        try {
            activity?.let {
                if (canGotoAppSystemSettingPage(it)) {
                    val intent = getAppSettingsIntent(it)
                    settingsLauncher?.launch(intent)
                } else {
                    gotoAppInfo()
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun gotoAppInfo() {
        try {
            activity?.let {
                if (canGotoAppInfoPage(it)) {
                    val intent = getAppInfoIntent(it)
                    settingsLauncher?.launch(intent)
                } else {
                    callbackResult()
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun callbackResult() {
        val isAllGranted = currentDenyList.isEmpty()
        requestCallbackList?.forEach {
            it.onResult(isAllGranted, currentGrantList, currentDenyList)
        }
    }

    private fun getNeverAskMessage(permissions: List<String>): String {
        val context = activity?: return ""
        val defaultMsg = context.getString(R.string.permission_dialog_never_ask_message,
            getPermissionInfoString(context, *permissions.toTypedArray()))
        return when (permissions.getOrNull(0)?.takeIf {
            permissions.size == 1
        }) {
            Manifest.permission.READ_PHONE_STATE -> PermissionB.sNeverAskDialogTextMap?.get(Manifest.permission.READ_PHONE_STATE)?: defaultMsg
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> PermissionB.sNeverAskDialogTextMap?.get(Manifest.permission.WRITE_EXTERNAL_STORAGE)?: defaultMsg
            Manifest.permission.CAMERA -> PermissionB.sNeverAskDialogTextMap?.get(Manifest.permission.CAMERA)?: defaultMsg
            else -> defaultMsg
        }
    }


}