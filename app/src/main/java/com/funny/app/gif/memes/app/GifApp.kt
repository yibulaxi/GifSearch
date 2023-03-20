package com.funny.app.gif.memes.app

//import com.allever.app.gif.search.ad.AdConstants
//import com.allever.app.gif.search.ad.AdFactory
import com.funny.app.gif.memes.function.maker.GifMakeHelper
import com.funny.app.gif.memes.function.media.FolderBean
import com.funny.app.gif.memes.function.media.MediaHelper
import com.funny.app.gif.memes.function.network.NetRepository
import com.funny.app.gif.memes.function.store.Store
import com.funny.app.gif.memes.util.ImgLoader

import com.funny.lib.common.app.App
import com.funny.lib.common.util.log
import com.funny.lib.common.util.loge
//import com.allever.lib.recommend.RecommendGlobal
//import com.allever.lib.umeng.UMeng
import com.xm.lib.BaseApp
import com.xm.lib.base.config.NetConfig
import com.xm.lib.datastroe.DataStore
import com.xm.netmodel.config.HttpConfig
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.litepal.LitePal


class GifApp : BaseApp() {
    override fun initThreadPackage() {

        App.context = this

        com.android.absbase.App.setContext(this)

        LitePal.initialize(this)


//        AdChainHelper.init(this, AdConstants.adData, AdFactory())

//        GiphyCoreUI.configure(this, "ENMowe3QQJBeL3fHproYw7C67ignSnuL")

//        RecommendGlobal.init(UMeng.getChannel())

        //初始化网络
        HttpConfig.init(applicationContext, "errorCode", "data")
            .setJsonMsgKeyName("errorMsg").setResponseOk(0)
            .setVerify(true)
            .builder("https://www.wanandroid.com/", 100)



        GlobalScope.launch {
            DataStore.init(App.context)
            val response = NetRepository.initGifFun(Store.getToken(), Store.getUserId().toString()) {
                loge(it)
            }
            response.data?.let {
                val token = it.token
                if (token.isNotEmpty()) {
                    Store.saveToken(it.token)
                    log("初始化成功： ${it.token}")
                }
            }

            val folderInfo = MediaHelper.getAllFolder(this@GifApp, MediaHelper.TYPE_VIDEO)
            folderInfo.add(FolderBean())
            folderInfo.map {
                log(it.dir)

                val mediaItemList = MediaHelper.getVideoMedia(this@GifApp, it.dir, 0)
                mediaItemList.map {
                    log("视频：${it.path}")
                }
            }

            val result = MediaHelper.getImageMedia(App.context, GifMakeHelper.gifDir)
            result.map {
                log("Gif: ${it.path}")
            }
        }

    }


    override fun initLoginStateConfig() = object : NetConfig() {
        override fun setLoginOutCallBack() {
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        ImgLoader.onLowMemory()
    }

    override fun onTerminate() {
        super.onTerminate()
        ImgLoader.clearMemoryCache()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        ImgLoader.onTrimMemroy(level)
    }
}