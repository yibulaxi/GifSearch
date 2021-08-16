
package com.allever.app.gif.search.ui.maker

import com.allever.app.gif.search.BR
import com.allever.app.gif.search.R
import com.allever.app.gif.search.app.BaseDataActivity2
import com.allever.app.gif.search.databinding.ActivityPickBinding
import com.allever.app.gif.search.ui.maker.model.PickViewModel
import com.xm.lib.base.config.DataBindingConfig

class PickActivity : BaseDataActivity2<ActivityPickBinding, PickViewModel>(){

    override fun initDataBindingConfig() = DataBindingConfig(R.layout.activity_pick, BR.pickViewModel)

    override fun initDataAndEvent() {
        mBinding.includeTopBar.tvLabel.text = getStringRes(R.string.choose_video)
        mBinding.includeTopBar.ivLeft.setOnClickListener {
            finish()
        }
    }

    override fun destroyView() {
    }

}