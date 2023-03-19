package com.allever.app.gif.memes.ui

import com.allever.app.gif.memes.BR
import com.allever.app.gif.memes.R
import com.allever.app.gif.memes.app.BaseDataActivity2
import com.allever.app.gif.memes.databinding.ActivityGifFunDebugBinding
import com.xm.lib.base.config.DataBindingConfig

class GifFunDebugActivity: BaseDataActivity2<ActivityGifFunDebugBinding, GifFunDebugViewModel>() {

    override fun initDataAndEvent() {

    }

    override fun destroyView() {
    }

    override fun initDataBindingConfig() = DataBindingConfig(R.layout.activity_gif_fun_debug, BR.gifFunDebugViewModel)
}