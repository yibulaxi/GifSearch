package com.allever.app.gif.search.ui.main.model

import com.allever.app.gif.search.ui.adapter.GifAdapter
import com.allever.app.gif.search.ui.adapter.bean.GifItem
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModelKt

class TrendViewModel: BaseViewModelKt<IBaseView>() {

    var gifDataList = mutableListOf<GifItem>()
    lateinit var adapter: GifAdapter



    override fun onCreated() {
    }
}