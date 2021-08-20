package com.allever.app.gif.search.function.maker

import android.content.Context
import android.net.Uri
import com.allever.app.gif.search.function.media.MediaHelper
import com.allever.lib.common.app.App
import com.allever.lib.common.util.FileUtil
import com.allever.lib.common.util.FileUtils
import com.allever.lib.common.util.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

object GifMakeHelper {

    var gifDir = "${App.context.externalCacheDir}${File.separator}gif"

    init {
        GlobalScope.launch {
            if (!FileUtils.checkExist(gifDir)) {
                FileUtil.createDir(gifDir)
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    suspend fun makeGif(
        context: Context,
        fromFile: String,
        toFile: String,
        fromPosition: Int,
        toPosition: Int,
        period: Int,
        progress: (current: Int, total: Int) -> Unit
    ) = withContext(Dispatchers.IO) {
        val maker = GifMaker(2)
        maker.setOnGifListener { current, total ->
            progress(current, total)
        }
        maker.makeGifFromVideo(
            context,
            Uri.parse(fromFile),
            fromPosition.toLong(),
            toPosition.toLong(),
            period.toLong(),
            toFile
        )
    }

}