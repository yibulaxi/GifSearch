package com.allever.app.giffun.app

import android.os.Environment
import com.allever.lib.common.app.App
import com.allever.lib.common.util.FileUtil
import com.allever.lib.common.util.FileUtils
import java.io.File

object Global {

    //保存路径 /sdcard/GifFunny/XXX.gif
    val saveDir = Environment.getExternalStorageDirectory().absoluteFile.path + File.separator + "GifFunny"

    //缓存下载完成后复制到的目录 /sdcard/{packageName}/temp/XXX
    val tempDir = Environment.getExternalStorageDirectory().absoluteFile.path + File.separator + App.context.packageName + File.separator + "GifFunny"

    //缓存时的路径,浏览时自动下载 /data/data/cache/gif/XXX
    val cacheDir = App.context.cacheDir.absolutePath + File.separator +"gif"


    const val SP_TYPE = "type"
    const val SP_KEYWORD = "keyword"
    const val SP_OFFSET = "offset"
    const val SP_COUNT = "count"


    fun init() {

    }

    fun createDir() {
        createDir(cacheDir)
        createDir(tempDir)
        createDir(saveDir)
    }

    private fun createDir(dir: String) {
        if (!FileUtils.checkExist(dir)) {
            FileUtil.createDir(dir)
        }
    }
}