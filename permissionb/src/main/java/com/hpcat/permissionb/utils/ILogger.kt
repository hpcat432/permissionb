package com.hpcat.permissionb.utils

import org.json.JSONObject

interface ILogger {

    fun log(eventName: String, jsonObject: JSONObject)

}