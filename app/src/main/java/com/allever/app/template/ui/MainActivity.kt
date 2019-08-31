package com.allever.app.template.ui

import com.allever.app.template.R
import com.allever.app.template.app.BaseActivity
import com.allever.app.template.ui.mvp.presenter.MainPresenter
import com.allever.app.template.ui.mvp.view.MainView

class MainActivity : BaseActivity<MainView, MainPresenter>(), MainView {
    override fun getContentView(): Any = R.layout.activity_main

    override fun initView() {
    }

    override fun initData() {
    }

    override fun createPresenter(): MainPresenter = MainPresenter()
}
