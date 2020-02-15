package com.allever.app.giffun.app

import com.allever.app.giffun.BuildConfig
import com.allever.app.giffun.ad.AdConstants
import com.allever.app.giffun.ad.AdFactory
import com.allever.lib.ad.chain.AdChainHelper
import com.allever.lib.common.app.App
import com.allever.lib.recommend.RecommendGlobal
import com.allever.lib.umeng.UMeng


class MyApp: App() {
    override fun onCreate() {
        super.onCreate()

        com.android.absbase.App.setContext(this)
        //初始化友盟
        if (!BuildConfig.DEBUG) {
            UMeng.init(this)
        }

        AdChainHelper.init(this, AdConstants.adData, AdFactory())

//        GiphyCoreUI.configure(this, "ENMowe3QQJBeL3fHproYw7C67ignSnuL")

        RecommendGlobal.init(UMeng.getChannel())

    }
}