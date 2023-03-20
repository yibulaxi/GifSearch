package com.funny.app.gif.memes.ui.search.model

import com.funny.app.gif.memes.ui.adapter.bean.GifItemBean
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModelKt

class SearchViewModel: BaseViewModelKt<IBaseView>() {

    var gifDataList = mutableListOf<GifItemBean>()
    override fun onCreated() {

    }
}