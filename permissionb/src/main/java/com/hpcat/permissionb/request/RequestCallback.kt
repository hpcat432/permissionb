package com.hpcat.permissionb.request

interface RequestCallback {

    /**
     * @param allGrated 是否全部授权成功
     * @param grantList 成功列表
     * @param denyList 失败列表
     */
    fun onResult(allGrated: Boolean, grantList: List<String>, denyList: List<Deny>)

}