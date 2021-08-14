package com.allever.app.gif.search.ui.main.model

import com.allever.app.gif.search.ui.GifFunDebugActivity
import com.allever.lib.recommend.RecommendActivity
import com.allever.lib.umeng.UMeng
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModelKt
import com.xm.lib.manager.IntentManager

class GifMainViewModel: BaseViewModelKt<IBaseView>() {
    override fun onCreated() {

    }

    fun onClickRecommend() {
        IntentManager.startActivity(mCxt, GifFunDebugActivity::class.java)
//        RecommendActivity.start(mCxt, UMeng.getChannel())
    }
}