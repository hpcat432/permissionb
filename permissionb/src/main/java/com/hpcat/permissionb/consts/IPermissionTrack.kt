package com.hpcat.permissionb.consts

object IPermissionTrack {

    object Action {
        const val EVENT_PERMISSION_REQUEST = "permission_request"
        const val EVENT_PERMISSION_REQUEST_RESULT = "permission_request_result"
        const val POPUPS = "popups"
        const val POPUPS_CLICK = "popups_click"
    }

    object Key {
        const val PERMISSION_NAME = "permission_name"
        const val PERMISSION_NEVER_ASK = "permission_never_ask"
        const val PERMISSION_REQUEST_RESULT = "request_result"
        const val TYPE = "type"
        const val ACTION = "action"
    }

    object Value {
        const val PERMISSION_DENY = "deny"
        const val PERMISSION_ALLOW = "allow"
        const val ENTER_SETTINGS = "enter_settings"
        const val CLOSE = "close"
    }

}