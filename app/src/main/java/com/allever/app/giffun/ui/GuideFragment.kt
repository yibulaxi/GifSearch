package com.allever.app.giffun.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.allever.app.giffun.R
import com.allever.lib.ad.chain.IAd
import com.allever.lib.common.app.App
import com.allever.lib.common.app.BaseFragment

class GuideFragment : BaseFragment() {
    private  var mBannerAd: IAd? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = LayoutInflater.from(App.context).inflate(R.layout.fragment_guide, container, false)

        val guideTips = StringBuilder()
        view.findViewById<TextView>(R.id.tvGuideTips).text = guideTips.toString()

//        mHandler.postDelayed({
//            val bannerContainer = view.findViewById<ViewGroup>(R.id.banner_container)
//            AdChainHelper.loadAd(AdConstants.AD_NAME_BANNER, bannerContainer, object : AdChainListener {
//                override fun onLoaded(ad: IAd?) {
//                    mBannerAd = ad
//                }
//                override fun onFailed(msg: String) {}
//                override fun onShowed() {}
//                override fun onDismiss() {}
//
//            })
//        }, 6000)


        return view
    }

    override fun onDestroy() {
        mBannerAd?.destroy()
        super.onDestroy()
    }
}