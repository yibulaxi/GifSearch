package com.allever.app.gif.search.ui.user.model

import androidx.lifecycle.MediatorLiveData
import com.allever.app.gif.search.ui.user.MineActivity
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModelKt
import com.xm.lib.manager.IntentManager

class RegisterViewModel: BaseViewModelKt<IBaseView>() {
    val nickname = MediatorLiveData<String>()
    override fun onCreated() {

    }

    fun onClickRegister() {
        IntentManager.startActivity(mCxt, MineActivity::class.java)
        finish()
    }
}