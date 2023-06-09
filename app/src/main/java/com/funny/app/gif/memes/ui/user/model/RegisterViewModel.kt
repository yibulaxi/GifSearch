package com.funny.app.gif.memes.ui.user.model

import android.text.TextUtils
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.funny.app.gif.memes.function.network.NetRepository
import com.funny.app.gif.memes.function.store.Store
import com.funny.lib.common.util.loge
import com.funny.lib.common.util.toast
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModelKt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterViewModel: BaseViewModelKt<IBaseView>() {
    var phone = ""
    var vCode = ""
    val nickname = MediatorLiveData<String>()
    override fun onCreated() {

    }

    fun onClickRegister() {
        if (TextUtils.isEmpty(nickname.value)) {
            toast("输入昵称")
            return
        }

        viewModelScope.launch(Dispatchers.Main) {
            val response = NetRepository.register(phone, vCode, nickname.value!!) {
                toast(it)
                loge(it)
            }
            response.data?.let {
                Store.saveToken(it.token)
                Store.saveUserId(it.userId)
                finish()
            }
        }

//        IntentManager.startActivity(mCxt, MineActivity::class.java)
//        finish()
    }
}