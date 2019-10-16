package com.allever.app.template.ui

import android.os.Bundle
import com.allever.app.template.R
import com.allever.lib.common.app.BaseActivity

class SplashActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        mHandler.postDelayed({
            MainActivity.start(this)
            finish()
        }, 2000)
    }
}