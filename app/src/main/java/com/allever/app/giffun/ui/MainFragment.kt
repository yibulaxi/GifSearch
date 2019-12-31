package com.allever.app.giffun.ui

import android.view.View
import com.allever.app.giffun.R
import com.allever.app.giffun.app.BaseFragment
import com.allever.app.giffun.ui.mvp.presenter.MainPresenter
import com.allever.app.giffun.ui.mvp.view.MainView

class MainFragment : BaseFragment<MainView, MainPresenter>(), MainView {
    override fun getContentView(): Int = R.layout.fragment_main

    override fun initView(root: View) {
    }

    override fun initData() {
    }

    override fun createPresenter(): MainPresenter = MainPresenter()

}