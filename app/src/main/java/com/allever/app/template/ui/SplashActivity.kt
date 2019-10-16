package com.allever.app.template.ui

import android.os.Bundle
import com.allever.app.template.R
import com.allever.lib.common.app.BaseActivity
import com.allever.lib.common.util.ActivityCollector

class SplashActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        mHandler.postDelayed({
            ActivityCollector.startActivity(this, MainTabActivity::class.java)
            finish()
        }, 2000)
    }
}