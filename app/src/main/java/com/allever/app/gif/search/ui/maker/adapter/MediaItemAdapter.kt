package com.allever.app.gif.search.ui.maker.adapter

import android.view.ViewGroup
import com.allever.app.gif.search.ui.maker.adapter.bean.MediaItem
import com.allever.app.gif.search.ui.maker.adapter.view.MediaItemView
import com.xm.lib.base.adapter.recyclerview.BaseRecyclerAdapter
import com.xm.lib.base.adapter.recyclerview.BaseViewHolder

class MediaItemAdapter : BaseRecyclerAdapter<MediaItem, BaseViewHolder<MediaItem>>() {
    override fun bindViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder<MediaItem> {
        return BaseViewHolder(MediaItemView(mContext))
    }
}