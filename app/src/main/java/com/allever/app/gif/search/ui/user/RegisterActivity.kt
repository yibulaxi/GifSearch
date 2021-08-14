package com.allever.app.gif.search.ui.user

import com.allever.app.gif.search.BR
import com.allever.app.gif.search.R
import com.allever.app.gif.search.app.BaseDataActivity2
import com.allever.app.gif.search.databinding.ActivityRegisterBinding
import com.allever.app.gif.search.ui.user.model.RegisterViewModel
import com.xm.lib.base.config.DataBindingConfig

class RegisterActivity: BaseDataActivity2<ActivityRegisterBinding, RegisterViewModel>() {

    override fun initDataBindingConfig() = DataBindingConfig(R.layout.activity_register, BR.registerViewModel)

    override fun initDataAndEvent() {
        mBinding.includeTapBar.tvLabel.text = getStringRes(R.string.register)
        mBinding.includeTapBar.ivLeft.setOnClickListener {
            finish()
        }
    }

    override fun destroyView() {
    }

}