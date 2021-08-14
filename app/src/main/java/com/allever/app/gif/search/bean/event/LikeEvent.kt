package com.allever.app.gif.search.bean.event

import com.allever.app.gif.search.ui.adapter.bean.GifItem

class LikeEvent {
    var id: String = ""
    var type: Int = 0
    var isLiked: Boolean = false
    var dataBean: GifItem? = null
}