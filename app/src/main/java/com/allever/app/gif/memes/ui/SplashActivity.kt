package com.allever.app.gif.memes.ui

import android.Manifest
import android.os.Bundle
import com.allever.app.gif.memes.BuildConfig
import com.allever.app.gif.memes.R
import com.allever.app.gif.memes.app.Global
import com.allever.app.gif.memes.function.store.Repository
import com.allever.app.gif.memes.ui.main.GifMainActivity
import com.allever.app.gif.memes.util.SpUtils
import com.allever.lib.common.app.BaseActivity
import com.allever.lib.common.util.ActivityCollector
import com.allever.lib.common.util.log
import com.allever.lib.permission.PermissionManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if (BuildConfig.DEBUG) {
            SpUtils.putString(Global.SP_OFFSET, "0")
            SpUtils.putString(Global.SP_SEARCH_OFFSET, "0")
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