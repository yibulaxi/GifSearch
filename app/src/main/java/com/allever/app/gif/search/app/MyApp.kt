package com.allever.app.gif.search.app

import com.allever.app.gif.search.BuildConfig
import com.allever.app.gif.search.ad.AdConstants
import com.allever.app.gif.search.ad.AdFactory
import com.allever.app.gif.search.util.ImageLoader
import com.allever.lib.ad.chain.AdChainHelper
import com.allever.lib.common.app.App
import com.allever.lib.recommend.RecommendGlobal
import com.allever.lib.umeng.UMeng
import com.xm.lib.BaseApp
import com.xm.lib.base.config.NetConfig
import com.xm.lib.datastroe.DataStore
import com.xm.netmodel.config.HttpConfig
import org.litepal.LitePal


class MyApp : BaseApp() {
    override fun initThreadPackage() {

        App.context = this

        com.android.absbase.App.setContext(this)

        LitePal.initialize(this)

        //初始化友盟
        if (!BuildConfig.DEBUG) {
            UMeng.init(this)
        }

        AdChainHelper.init(this, AdConstants.adData, AdFactory())

//        GiphyCoreUI.configure(this, "ENMowe3QQJBeL3fHproYw7C67ignSnuL")

        RecommendGlobal.init(UMeng.getChannel())

        //初始化网络
        HttpConfig.init(applicationContext, "errorCode", "data")
            .setJsonMsgKeyName("errorMsg").setResponseOk(0)
            .setVerify(true)
            .builder("https://www.wanandroid.com/", 100)

        DataStore.init(this)

    }


    override fun initLoginStateConfig() = object : NetConfig() {
        override fun setLoginOutCallBack() {
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        ImageLoader.onLowMemory()
    }

    override fun onTerminate() {
        super.onTerminate()
        ImageLoader.clearMemoryCache()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        ImageLoader.onTrimMemroy(level)
    }
}