package com.allever.app.template.app

import com.allever.app.template.BuildConfig
import com.allever.lib.common.app.App
import com.allever.lib.umeng.UMeng

class MyApp: App() {
    override fun onCreate() {
        super.onCreate()

        com.android.absbase.App.setContext(this)
        BuildConfig.UMENG_APP_KEY
        //初始化友盟
        UMeng.init(this, channel = "text")
    }
}