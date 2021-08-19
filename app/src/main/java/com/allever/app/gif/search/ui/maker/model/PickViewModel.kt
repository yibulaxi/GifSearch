package com.allever.app.gif.search.ui.maker.model

import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import com.allever.app.gif.search.function.maker.GifMakeHelper
import com.allever.app.gif.search.function.media.MediaBean
import com.allever.app.gif.search.function.media.MediaHelper
import com.allever.app.gif.search.ui.maker.adapter.MediaItemAdapter
import com.allever.app.gif.search.ui.maker.adapter.bean.MediaItem
import com.allever.lib.common.util.log
import com.allever.lib.common.util.toast
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModelKt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class PickViewModel: BaseViewModelKt<IBaseView>() {
    lateinit var adapter : MediaItemAdapter

    lateinit var layoutManager: GridLayoutManager

    private val mediaItemList = mutableListOf<MediaItem>()
    val mediaItemListLiveData = MutableLiveData<List<MediaItem>>()
    var lastPosition = 0

    override fun onCreated() {
        layoutManager = GridLayoutManager(mCxt, 3)
        adapter = MediaItemAdapter()
        adapter.setOnItemClickedListener { v, position, item ->
            if (lastPosition == position) {
                item.selected = !item.selected
            } else {
                mediaItemList[lastPosition].selected = false
                adapter.notifyItemChanged(lastPosition, lastPosition)
                item.selected = !item.selected
                lastPosition = position
            }
            adapter.notifyItemChanged(position, position)

            if (item.selected) {
                item.data?.path?.let {
                    viewModelScope.launch(Dispatchers.Main) {
                        val result = GifMakeHelper.makeGif(mCxt, it, "${mCxt.cacheDir}${File.separator}.gif", 0,  item.data?.duration?.toInt()?:0, 200) { current, total ->
                            log("$current/$total")
                        }
                        log("完成：$result")
                    }
                }


            }
        }

        adapter.setOnItemLongClickedListener{ v, position, item ->
            toast(item?.data?.path?:"")
            return@setOnItemLongClickedListener true
        }

        viewModelScope.launch {
            val allVideo = MediaHelper.getVideoMedia(mCxt, "", 0)
            allVideo.map {
                val mediaItem = MediaItem()
                mediaItem.data = it
                mediaItemList.add(mediaItem)
            }
            mediaItemListLiveData.value = mediaItemList
        }
    }
}