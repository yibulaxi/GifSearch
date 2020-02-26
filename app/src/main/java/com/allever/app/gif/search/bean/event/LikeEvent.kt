package com.allever.app.gif.search.bean.event

import com.allever.app.gif.search.bean.DataBean

class LikeEvent {
    var id: String = ""
    var isLiked: Boolean = false
    var dataBean: DataBean? = null
}