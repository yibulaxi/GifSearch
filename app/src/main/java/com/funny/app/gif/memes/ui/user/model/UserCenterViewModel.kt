package com.funny.app.gif.memes.ui.user.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.funny.app.gif.memes.app.Global
import com.funny.app.gif.memes.function.network.NetRepository
import com.funny.app.gif.memes.function.store.Store
import com.funny.lib.common.util.toast
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModelKt
import com.xm.lib.util.loge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserCenterViewModel: BaseViewModelKt<IBaseView>() {
    val nickname = MutableLiveData<String>()
    val phone = MutableLiveData<String>()

    override fun onCreated() {
        viewModelScope.launch(Dispatchers.Main) {
            val response = NetRepository.getUserInfo(Store.getToken(), Store.getUserId().toString()) {
                loge(it)
                toast(it)
            }

            response.data?.let {
                nickname.value = it.nickname
                phone.value = Store.getPhone()
            }
        }
    }

    fun onClickLogout() {
        Global.logout()
        finish()
    }
}