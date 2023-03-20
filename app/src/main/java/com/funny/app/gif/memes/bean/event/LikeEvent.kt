package com.funny.app.gif.memes.bean.event

import com.funny.app.gif.memes.ui.adapter.bean.GifItem

class LikeEvent {
    var id: String = ""
    var type: Int = 0
    var isLiked: Boolean = false
    var dataBean: GifItem? = null
}