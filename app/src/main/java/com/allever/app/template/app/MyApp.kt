package com.allever.app.template.app

import com.allever.app.template.ad.AdConstants
import com.allever.app.template.ad.AdFactory
import com.allever.lib.ad.chain.AdChainHelper
import com.allever.lib.common.app.App
import com.allever.lib.umeng.UMeng

class MyApp: App() {
    override fun onCreate() {
        super.onCreate()

        com.android.absbase.App.setContext(this)
        //初始化友盟
        UMeng.init(this)

        AdChainHelper.init(this, AdConstants.adData, AdFactory())
    }
}