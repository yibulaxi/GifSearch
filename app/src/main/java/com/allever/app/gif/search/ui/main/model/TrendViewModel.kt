package com.allever.app.gif.search.ui.main.model

import androidx.lifecycle.viewModelScope
import com.allever.app.gif.search.bean.DataBean
import com.allever.app.gif.search.function.network.NetRepository
import com.allever.app.gif.search.ui.adapter.GifAdapter
import com.allever.lib.common.util.log
import com.allever.lib.common.util.loge
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModelKt
import kotlinx.coroutines.launch

class TrendViewModel: BaseViewModelKt<IBaseView>() {

    var gifDataList = mutableListOf<DataBean>()
    lateinit var adapter: GifAdapter



    override fun onCreated() {
        viewModelScope.launch {
            val response = NetRepository.getTrendList(0) {
                loge(it)
            }
            response.data?.let {
                log(it.toString())
            }
        }
    }
}