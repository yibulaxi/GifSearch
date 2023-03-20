package com.funny.app.gif.memes.ui

import com.funny.app.gif.memes.BR
import com.funny.app.gif.memes.R
import com.funny.app.gif.memes.app.BaseDataActivity2
import com.funny.app.gif.memes.databinding.ActivityGifFunDebugBinding
import com.xm.lib.base.config.DataBindingConfig

class GifFunDebugActivity: BaseDataActivity2<ActivityGifFunDebugBinding, GifFunDebugViewModel>() {

    override fun initDataAndEvent() {

    }

    override fun destroyView() {
    }

    override fun initDataBindingConfig() = DataBindingConfig(R.layout.activity_gif_fun_debug, BR.gifFunDebugViewModel)
}