package com.funny.app.gif.memes.app

import android.os.Environment
import com.funny.app.gif.memes.function.store.Store
import com.funny.app.gif.memes.ui.adapter.bean.GifItemBean
import com.funny.lib.common.app.App
import com.funny.lib.common.util.FileUtil
import com.funny.lib.common.util.FileUtils
import java.io.File

object GlobalObj {

    //备份
    val backFileDir =
        Environment.getExternalStorageDirectory().absolutePath + File.separator + App.context.packageName + File.separator + "backup"

    val backupFilePath = backFileDir + File.separator + "data.json"

    //保存路径 /sdcard/GifFunny/XXX.gif
    val saveDir =
        Environment.getExternalStorageDirectory().absoluteFile.path + File.separator + "GifSearch"

    //缓存下载完成后复制到的目录 /sdcard/{packageName}/temp/XXX
    val tempDir =
        Environment.getExternalStorageDirectory().absoluteFile.path + File.separator + App.context.packageName + File.separator + ".temp"

    //缓存时的路径,浏览时自动下载 /data/data/cache/gif/XXX
    val cacheDir = App.context.cacheDir.absolutePath + File.separator + "gif"


    const val SP_TYPE = "type"
    const val SP_KEYWORD = "keyword"
    const val SP_OFFSET = "offset"
    const val SP_SEARCH_OFFSET = "search_offset"
    const val SP_COUNT = "count"

    const val SHOW_COUNT = 5

    fun init() {

    }

    fun createDir() {
        createDir(cacheDir)
        createDir(tempDir)
        createDir(saveDir)
        createDir(backFileDir)
    }

    fun getIndex(gifId: String, dataBeanList: MutableList<GifItemBean>): Int {
        var position = -1
        dataBeanList.mapIndexed { index, dataBean ->
            if (dataBean.id.toString() == gifId) {
                position = index
                return@mapIndexed
            }
        }
        return position
    }

    fun checkLogin(): Boolean = Store.getToken().isNotEmpty() && Store.getUserId() != 0

    fun logout() {
        Store.saveUserId(0)
        Store.saveToken("")
    }

    private fun createDir(dir: String) {
        if (!FileUtils.checkExist(dir)) {
            FileUtil.createDir(dir)
        }
    }

}