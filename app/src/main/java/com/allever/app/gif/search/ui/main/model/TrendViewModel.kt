package com.allever.app.gif.search.ui.main.model

import androidx.lifecycle.viewModelScope
import com.allever.app.gif.search.bean.DataBean
import com.allever.app.gif.search.function.network.NetRepository
import com.allever.app.gif.search.ui.adapter.GifAdapter
import com.allever.app.gif.search.ui.adapter.bean.GifItem
import com.allever.lib.common.util.log
import com.allever.lib.common.util.loge
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModelKt
import kotlinx.coroutines.launch

class TrendViewModel: BaseViewModelKt<IBaseView>() {

    var gifDataList = mutableListOf<GifItem>()
    lateinit var adapter: GifAdapter



    override fun onCreated() {
        return
        viewModelScope.launch {
            val response = NetRepository.getTrendList(0) {
                loge(it)
            }
            response.data?.let {
                log(it.toString())
            }

            try {

                val initResponse = NetRepository.initGifFun("", "")
                if (initResponse.status == 0) {
                    log("初始化成功: ${initResponse.status} -> ${initResponse.msg}" )
                } else {
                    loge("初始化失败: ${initResponse.status} -> ${initResponse.msg}" )
                }

                val vCodeResponse = NetRepository.fetchVCode("13434334310")
                if (vCodeResponse.status == 0) {
                    log("获取验证码成功: ${vCodeResponse.status} -> ${vCodeResponse.msg}" )
                } else {
                    loge("获取验证码失败: ${vCodeResponse.status} -> ${vCodeResponse.msg}" )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                log("获取验证码失败：${e.message}")
            }

        }
    }
}