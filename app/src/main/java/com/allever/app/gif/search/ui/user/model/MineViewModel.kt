package com.allever.app.gif.search.ui.user.model

import androidx.lifecycle.MutableLiveData
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModelKt

class MineViewModel: BaseViewModelKt<IBaseView>() {
    val nickname = MutableLiveData<String>()
    override fun onCreated() {
    }

    fun onClickLogout() {
        finish()
    }

    fun onClickBack() {

    }
}