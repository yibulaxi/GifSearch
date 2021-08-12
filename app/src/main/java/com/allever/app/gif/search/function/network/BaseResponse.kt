package com.allever.app.gif.search.function.network

open class BaseResponse<T>() {
    var `data`: T? = null
    var errorCode: Int = 0
    var errorMsg: String = ""
}