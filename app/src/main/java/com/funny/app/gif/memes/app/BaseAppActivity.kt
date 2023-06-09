package com.funny.app.gif.memes.app

import android.os.Bundle
import android.view.View
import com.funny.lib.common.mvp.BaseMvpActivity
import com.funny.lib.common.mvp.BasePresenter
import java.lang.RuntimeException

abstract class BaseAppActivity<V, P : BasePresenter<V>> : BaseMvpActivity<V, P>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (
            val contentView = getContentView()) {
            is Int -> {
                setContentView(contentView)
            }
            is View -> {
                setContentView(contentView)
            }
            else -> {
                throw RuntimeException("Please check contentView type")
            }
        }

        initView()
        initData()
    }

    abstract fun getContentView(): Any
    abstract fun initView()
    abstract fun initData()
}