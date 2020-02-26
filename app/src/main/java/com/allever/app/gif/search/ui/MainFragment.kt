package com.allever.app.gif.search.ui

import android.view.View
import com.allever.app.gif.search.R
import com.allever.app.gif.search.app.BaseFragment
import com.allever.app.gif.search.ui.mvp.presenter.MainPresenter
import com.allever.app.gif.search.ui.mvp.view.MainView

class MainFragment : BaseFragment<MainView, MainPresenter>(), MainView {
    override fun getContentView(): Int = R.layout.fragment_main

    override fun initView(root: View) {
    }

    override fun initData() {
    }

    override fun createPresenter(): MainPresenter = MainPresenter()

}