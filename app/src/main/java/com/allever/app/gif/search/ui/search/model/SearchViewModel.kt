package com.allever.app.gif.search.ui.search.model

import com.allever.app.gif.search.ui.adapter.bean.GifItem
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModelKt

class SearchViewModel: BaseViewModelKt<IBaseView>() {

    var gifDataList = mutableListOf<GifItem>()
    override fun onCreated() {

    }
}