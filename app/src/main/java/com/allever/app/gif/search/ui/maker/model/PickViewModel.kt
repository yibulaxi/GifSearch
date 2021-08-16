package com.allever.app.gif.search.ui.maker.model

import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import com.allever.app.gif.search.ui.maker.adapter.MediaItemAdapter
import com.allever.app.gif.search.ui.maker.adapter.bean.MediaItem
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModelKt

class PickViewModel: BaseViewModelKt<IBaseView>() {
    lateinit var adapter : MediaItemAdapter

    lateinit var layoutManager: GridLayoutManager

    private val mediaItemList = mutableListOf<MediaItem>()
    val mediaItemListLiveData = MutableLiveData<List<MediaItem>>()

    override fun onCreated() {
        layoutManager = GridLayoutManager(mCxt, 3)
        adapter = MediaItemAdapter()
        for (i in 0 .. 30) {
            val mediaItem = MediaItem()
            mediaItemList.add(mediaItem)
        }
        mediaItemListLiveData.value = mediaItemList
    }
}