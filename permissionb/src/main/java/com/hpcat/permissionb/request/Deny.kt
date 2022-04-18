package com.hpcat.permissionb.request

class Deny(var permission: String, var isNeverAsk: Boolean) {
    override fun toString(): String {
        return "<$permission : $isNeverAsk>"
    }
}