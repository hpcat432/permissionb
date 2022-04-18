package com.hpcat.permissionb.request

/**
 * Request以责任链形式运作，每个节点收集单个权限信息，执行各自任务，并在
 * 链执行结束时统一请求所有权限
 */
interface Request {

    var permission: String

    fun request(requestList: MutableList<AbsActualRequest>)

}