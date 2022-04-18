package com.hpcat.permissionb.request

abstract class AbsWrapRequest: Request {

    override var permission: String = ""

    abstract var originRequest: Request
    abstract var callbackList: MutableList<RequestCallback>

}