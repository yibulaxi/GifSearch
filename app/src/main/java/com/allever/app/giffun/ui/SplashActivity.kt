package com.allever.app.giffun.ui

import android.os.Bundle
import android.os.Debug
import com.allever.app.giffun.BuildConfig
import com.allever.app.giffun.R
import com.allever.app.giffun.app.Global
import com.allever.app.giffun.util.SpUtils
import com.allever.lib.common.app.BaseActivity
import com.allever.lib.common.util.ActivityCollector

class SplashActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if (BuildConfig.DEBUG) {
            SpUtils.putString(Global.SP_OFFSET , "0")
            SpUtils.putString(Global.SP_SEARCH_OFFSET, "0")
        }
        mHandler.postDelayed({
            ActivityCollector.startActivity(this, GifMainActivity::class.java)
            finish()
        }, 2000)
    }
}