package com.allever.app.gif.search.ui.maker

import com.allever.app.gif.search.BR
import com.allever.app.gif.search.R
import com.allever.app.gif.search.app.BaseFragment2
import com.allever.app.gif.search.databinding.FragmentMineBinding
import com.allever.app.gif.search.ui.maker.model.MineViewModel
import com.xm.lib.base.config.DataBindingConfig

class MineFragment: BaseFragment2<FragmentMineBinding, MineViewModel>() {
    override fun initDataBindingConfig() = DataBindingConfig(R.layout.fragment_mine, BR.mineViewModel)

    override fun initDataAndEvent() {
    }

    override fun destroyView() {
    }
}