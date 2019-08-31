package com.allever.app.template.ui

import android.view.View
import com.allever.app.template.R
import com.allever.app.template.app.BaseFragment
import com.allever.app.template.ui.mvp.presenter.MainPresenter
import com.allever.app.template.ui.mvp.view.MainView

class MainFragment : BaseFragment<MainView, MainPresenter>(), MainView {
    override fun getContentView(): Int = R.layout.fragment_main

    override fun initView(root: View) {
    }

    override fun initData() {
    }

    override fun createPresenter(): MainPresenter = MainPresenter()

}