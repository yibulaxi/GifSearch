package com.funny.app.gif.memes.ui.main.model

import com.funny.app.gif.memes.ui.adapter.GifItemAdapter
import com.funny.app.gif.memes.ui.adapter.bean.GifItemBean
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModelKt

class HotViewModel: BaseViewModelKt<IBaseView>() {

    var gifDataList = mutableListOf<GifItemBean>()
    lateinit var adapter: GifItemAdapter



    override fun onCreated() {
    }
}