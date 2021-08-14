package com.allever.app.gif.search.ui

import android.service.autofill.Dataset
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.allever.app.gif.search.function.network.NetRepository
import com.allever.app.gif.search.function.store.Store
import com.allever.lib.common.util.log
import com.allever.lib.common.util.loge
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModelKt
import com.xm.lib.datastroe.DataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GifFunDebugViewModel : BaseViewModelKt<IBaseView>() {

    val debugText by lazy { MutableLiveData<String>() }

    val vCode by lazy { MutableLiveData<String>() }

    val phone by lazy { MutableLiveData<String>() }

    val nickname by lazy { MutableLiveData<String>() }

    val lastFeed by lazy { MutableLiveData<String>() }
    override fun onCreated() {
        phone.value = "13434334310"
    }

    fun onClickInit() {
        viewModelScope.launch {
            val response = NetRepository.initGifFun(Store.getToken(), Store.getUserId().toString())
            debugText.value = "初始化：${response.status} -> ${response.msg}"
            if (response.status == 0) {
                val token = response.token
                if (token.isNotEmpty()) {
                    Store.saveToken(response.token)
                    debugText.value = "初始化 token = $token"
                }
            }
        }
    }

    fun onClickFetchWorld() {
        viewModelScope.launch {
            val lastFeetValue = lastFeed.value?:""
            val response = NetRepository.fetchWorld(Store.getToken(), Store.getUserId().toString(), lastFeetValue)
            debugText.value = "Gif列表：${response.errorCode} -> ${response.errorMsg}"

            response.let {
                it.data?.feeds?.let {
                    it.map {
                        log("feedId: ${it.feedId} -> ${it.gif}")
                    }
                    lastFeed.value = it.last().feedId.toString()
                }
            }
        }
    }

    fun onClickFetchVCode() {
        if (phone.value?.isEmpty() == true) {
            debugText.value = "输入手机号"
            return
        }

        viewModelScope.launch {
            phone.value?.let {
                val response = NetRepository.fetchVCode(it)
                debugText.value = "验证码：${response.status} -> ${response.msg}"
            }
        }
    }

    fun onClickLogin() {
        if (phone.value?.isEmpty() == true) {
            debugText.value = "输入手机号"
            return
        }

        if (vCode.value?.isEmpty() == true) {
            debugText.value = "输入验证码"
            return
        }

        val token = Store.getToken()
//        if (token.isNotEmpty()) {
//            debugText.value = "已登录: $token"
//            //{"status":0,"msg":"OK","token":"3E7EDDBA8F6443CEBF2C6FF0258B197F","user_id":29542}
//
//            return
//        }

        viewModelScope.launch {
            val response = NetRepository.login(phone.value!!, vCode.value!!)
            debugText.value = "登录：${response.status} -> ${response.msg}"
            if (response.status == 0) {
                Store.saveToken(response.token)
                Store.saveUserId(response.userId)
            }

        }
    }

    fun onClickRegister() {
        if (phone.value?.isEmpty() == true) {
            debugText.value = "输入手机号"
            return
        }

        if (vCode.value?.isEmpty() == true) {
            debugText.value = "输入验证码"
            return
        }

        if (nickname.value?.isEmpty() == true) {
            debugText.value = "输入昵称"
            return
        }

        viewModelScope.launch {
            val response = NetRepository.register(phone.value!!, vCode.value!!, nickname.value!!)
            debugText.value = "注册：${response.status} -> ${response.msg}"
            if (response.status == 0) {
                Store.saveToken(response.token)
                Store.saveUserId(response.userId.toLong())
            }
        }
    }
}