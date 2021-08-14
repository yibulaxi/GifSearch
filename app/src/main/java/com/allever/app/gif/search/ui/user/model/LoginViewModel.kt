package com.allever.app.gif.search.ui.user.model

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.allever.app.gif.search.ui.user.RegisterActivity
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModelKt
import com.xm.lib.manager.IntentManager

class LoginViewModel: BaseViewModelKt<IBaseView>() {
    val phone = MediatorLiveData<String>()
    val vCode = MutableLiveData<String>()
    override fun onCreated() {

    }

    fun onClickFetchVCode() {

    }

    fun onClickLogin() {
        IntentManager.startActivity(mCxt, RegisterActivity::class.java)
        finish()
    }
}