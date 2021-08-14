package com.allever.app.gif.search.ui.user.model

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import com.allever.app.gif.search.app.Global
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModelKt

class MineViewModel: BaseViewModelKt<IBaseView>() {
    val nickname = MutableLiveData<String>()

    override fun onCreated() {
    }

    fun onClickLogout() {
        Global.logout()
        finish()
    }
}