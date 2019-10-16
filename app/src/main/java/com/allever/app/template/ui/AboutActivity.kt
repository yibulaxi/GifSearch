package com.allever.app.template.ui

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import com.allever.app.template.R
import com.allever.app.template.app.BaseActivity
import com.allever.app.template.ui.mvp.presenter.AboutPresenter
import com.allever.app.template.ui.mvp.view.AboutView
import com.allever.app.template.util.SystemUtils
import com.allever.lib.common.app.App

class AboutActivity: BaseActivity<AboutView, AboutPresenter>(), AboutView, View.OnClickListener {


    override fun getContentView(): Any = R.layout.activity_about

    override fun initView() {
        findViewById<View>(R.id.about_privacy).setOnClickListener(this)
        findViewById<View>(R.id.iv_left).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.about)

    }

    override fun initData() {
    }

    override fun createPresenter(): AboutPresenter = AboutPresenter()

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.iv_left -> {
                finish()
            }
            R.id.about_privacy -> {
//                val privacyUrl = "https://plus.google.com/116794250597377070773/posts/SYoEZWDm77x"
//                SystemUtils.startWebView(App.context, privacyUrl)
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