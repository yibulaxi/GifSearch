package com.allever.app.gif.search.ui

import com.allever.app.gif.search.BR
import com.allever.app.gif.search.R
import com.allever.app.gif.search.app.BaseDataActivity2
import com.allever.app.gif.search.databinding.ActivityGifFunDebugBinding
import com.xm.lib.base.config.DataBindingConfig

class GifFunDebugActivity: BaseDataActivity2<ActivityGifFunDebugBinding, GifFunDebugViewModel>() {

    override fun initDataAndEvent() {

    }

    override fun destroyView() {
    }

    override fun initDataBindingConfig() = DataBindingConfig(R.layout.activity_gif_fun_debug, BR.gifFunDebugViewModel)
}