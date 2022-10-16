package com.allever.app.gif.search.ui

import android.Manifest
import android.os.Bundle
import com.allever.app.gif.search.BuildConfig
import com.allever.app.gif.search.R
import com.allever.app.gif.search.app.Global
import com.allever.app.gif.search.function.store.Repository
import com.allever.app.gif.search.ui.main.GifMainActivity
import com.allever.app.gif.search.util.SpUtils
import com.allever.lib.common.app.BaseActivity
import com.allever.lib.common.util.ActivityCollector
import com.allever.lib.common.util.log
import com.allever.lib.permission.PermissionCompat
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
            if (!PermissionCompat.hasPermission(
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