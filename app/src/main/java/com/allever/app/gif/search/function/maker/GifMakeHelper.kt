package com.allever.app.gif.search.function.maker

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GifMakeHelper {

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