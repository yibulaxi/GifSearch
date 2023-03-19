package com.allever.app.gif.search.ui

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import com.allever.app.gif.search.BuildConfig
import com.allever.app.gif.search.R
//import com.allever.app.gif.search.ad.AdConstants
import com.allever.app.gif.search.app.BaseActivity
import com.allever.app.gif.search.ui.mvp.presenter.AboutPresenter
import com.allever.app.gif.search.ui.mvp.view.AboutView
import com.allever.lib.common.app.App
import com.allever.lib.common.util.SystemUtils
//import com.allever.lib.umeng.UMeng

class AboutActivity : BaseActivity<AboutView, AboutPresenter>(), AboutView, View.OnClickListener {


    override fun getContentView(): Any = R.layout.activity_about

    override fun initView() {
        findViewById<View>(R.id.about_privacy).setOnClickListener(this)
        findViewById<View>(R.id.iv_left).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.about)
//        val channel = UMeng.getChannel()
        val channel = "google"
        val last = if (BuildConfig.DEBUG) {
            "(Debug)-$channel\n" +
                    "${App.context.packageName}\n"
//                    "AdMob-${AdConstants.ADMOB_APP_ID}"
        } else {
            if (channel == "ad") {
                "(Release)-$channel\n" +
                        "${App.context.packageName}\n"
//                        "AdMob-${AdConstants.ADMOB_APP_ID}"
            } else {
                ""
            }
        }
        findViewById<TextView>(R.id.about_app_version).text = "v${BuildConfig.VERSION_NAME}$last"
        findViewById<TextView>(R.id.about_right).text =
            String.format(getString(R.string.about_right), getString(R.string.app_name))
    }

    override fun initData() {
    }

    override fun createPresenter(): AboutPresenter = AboutPresenter()

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_left -> {
                finish()
            }
            R.id.about_privacy -> {
                val privacyUrl = "http://x.xiniubaba.com/x.php/E1lSY4/3123"
                SystemUtils.startWebView(App.context, privacyUrl)
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AboutActivity::class.java)
            context.startActivity(intent)
        }
    }
}