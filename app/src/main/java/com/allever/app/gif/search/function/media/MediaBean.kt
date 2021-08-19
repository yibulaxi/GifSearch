package com.allever.app.gif.search.function.media

import android.net.Uri

class MediaBean {
    var path = ""
    var date: Long = 0
    var degree: Int = 0
    var uri: Uri? = null
    var type: Int = MediaType.TYPE_OTHER_IMAGE
    var duration: Long = 0
}