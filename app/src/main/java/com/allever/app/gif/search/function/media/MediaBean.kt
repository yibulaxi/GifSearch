package com.allever.app.gif.search.function.media

import android.net.Uri

class MediaBean {
    var path = ""
        set(value) {
            var fileName = value
            val array = value.split("/")
            if (array.isNotEmpty()){
                fileName = array[array.size - 1]
                if (fileName.contains(".")) {
                    fileName = "${fileName.split(".")[0]}.gif"
                }
            }
            this.name = fileName
            field = value
        }
    var date: Long = 0
    var degree: Int = 0
    var uri: Uri? = null
    var type: Int = MediaType.TYPE_OTHER_IMAGE
    var duration: Long = 0
    var name: String = ""
}