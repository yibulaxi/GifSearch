package com.allever.app.gif.search.ui.user

import com.allever.app.gif.search.BR
import com.allever.app.gif.search.R
import com.allever.app.gif.search.app.BaseDataActivity2
import com.allever.app.gif.search.databinding.ActivityMineBinding
import com.allever.app.gif.search.ui.user.model.MineViewModel
import com.xm.lib.base.config.DataBindingConfig

class MineActivity : BaseDataActivity2<ActivityMineBinding, MineViewModel>(){

    override fun initDataBindingConfig() = DataBindingConfig(R.layout.activity_mine, BR.mineViewModel)

    override fun initDataAndEvent() {
        mBinding.includeTapBar.tvLabel.text = getStringRes(R.string.mine)
        mBinding.includeTapBar.ivLeft.setOnClickListener {
            finish()
        }
    }

    override fun destroyView() {
    }

}