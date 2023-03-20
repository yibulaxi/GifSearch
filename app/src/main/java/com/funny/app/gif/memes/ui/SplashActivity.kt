package com.funny.app.gif.memes.ui

import android.Manifest
import android.os.Bundle
import com.funny.app.gif.memes.BuildConfig
import com.funny.app.gif.memes.R
import com.funny.app.gif.memes.app.Global
import com.funny.app.gif.memes.function.store.Repository
import com.funny.app.gif.memes.ui.main.GifMainActivity
import com.funny.app.gif.memes.util.SpHelper
import com.funny.lib.common.app.BaseActivity
import com.funny.lib.common.util.ActivityCollector
import com.funny.lib.common.util.log
import com.funny.lib.permission.PermissionManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if (BuildConfig.DEBUG) {
            SpHelper.putString(Global.SP_OFFSET, "0")
            SpHelper.putString(Global.SP_SEARCH_OFFSET, "0")
        }
        mHandler.postDelayed({
            ActivityCollector.startActivity(this, GifMainActivity::class.java)
            finish()
        }, 2000)

        GlobalScope.launch {
            if (!PermissionManager.hasPermission(
                    this@SplashActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                return@launch
            }
            val result = Repository.getMyGif()
            result.map {
                log("Gif: ${it.url}")
            }
        }
    }
}