package com.funny.app.gif.memes.ui.main.model

import com.funny.app.gif.memes.ui.adapter.GifItemAdapter
import com.funny.app.gif.memes.ui.adapter.bean.GifItem
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModelKt

class TrendViewModel: BaseViewModelKt<IBaseView>() {

    var gifDataList = mutableListOf<GifItem>()
    lateinit var adapter: GifItemAdapter



    override fun onCreated() {
    }
}