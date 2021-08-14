package com.allever.app.gif.search.ui.main

import android.animation.ObjectAnimator
import android.app.Dialog
import android.graphics.PorterDuff
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.allever.app.gif.search.BR
import com.allever.app.gif.search.R
import com.allever.app.gif.search.ad.AdConstants
import com.allever.app.gif.search.app.BaseDataActivity2
import com.allever.app.gif.search.databinding.ActivityGifMainBinding
import com.allever.app.gif.search.function.download.DownloadManager
import com.allever.app.gif.search.ui.*
import com.allever.app.gif.search.ui.TabModel
import com.allever.app.gif.search.ui.adapter.ViewPagerAdapter
import com.allever.app.gif.search.ui.main.model.GifMainViewModel
import com.allever.app.gif.search.util.ImageLoader
import com.allever.lib.ad.chain.AdChainHelper
import com.allever.lib.ad.chain.AdChainListener
import com.allever.lib.ad.chain.IAd
import com.allever.lib.comment.CommentHelper
import com.allever.lib.comment.CommentListener
import com.allever.lib.common.app.BaseFragment
import com.allever.lib.common.ui.widget.tab.TabLayout
import com.allever.lib.common.util.ActivityCollector
import com.allever.lib.common.util.DisplayUtils
import com.allever.lib.recommend.RecommendActivity
import com.allever.lib.recommend.RecommendDialogHelper
import com.allever.lib.recommend.RecommendDialogListener
import com.allever.lib.recommend.RecommendGlobal
import com.allever.lib.ui.widget.ShakeHelper
import com.allever.lib.umeng.UMeng
import com.xm.lib.base.config.DataBindingConfig
import com.xm.lib.util.HandlerHelper
import kotlinx.android.synthetic.main.activity_gif_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GifMainActivity : BaseDataActivity2<ActivityGifMainBinding, GifMainViewModel>(), View.OnClickListener, TabLayout.OnTabSelectedListener {

    private lateinit var mVp: ViewPager
    private lateinit var mViewPagerAdapter: ViewPagerAdapter
    private lateinit var mTab: TabLayout
    private var mainTabHighlight = 0
    private var mainTabUnSelectColor = 0

    private var mFragmentList = mutableListOf<Fragment>()

    private var mShakeAnimator: ObjectAnimator? = null

    private var mBannerAd: IAd? = null
    private var mExitInsertAd: IAd? = null


    override fun initDataBindingConfig() = DataBindingConfig(R.layout.activity_gif_main, BR.gifMainViewModel)

    override fun initDataAndEvent() {
        ivRight.setOnClickListener(this)
        ivRecommend.setOnClickListener(this)
        mShakeAnimator = ShakeHelper.createShakeAnimator(ivRecommend, true)
        mShakeAnimator?.start()

        mTab = findViewById(R.id.tab_layout)
        mVp = findViewById(R.id.id_main_vp)

        mainTabHighlight = resources.getColor(R.color.main_tab_highlight)
        mainTabUnSelectColor = resources.getColor(R.color.main_tab_unselect_color)

        initViewPagerData()
        initViewPager()
        initTab()

        HandlerHelper.mainHandler.postDelayed({
            loadBanner()
            loadExitInsert()
        }, 10000)
    }


    override fun destroyView() {
    }


    private fun initViewPagerData() {
        mFragmentList.add(TrendFragment())
        mFragmentList.add(SearchFragment())
        mFragmentList.add(LikedFragment())
        mViewPagerAdapter = ViewPagerAdapter(supportFragmentManager, mFragmentList)
    }

    private fun initViewPager() {
        mVp.offscreenPageLimit = 3
        mVp.adapter = mViewPagerAdapter

        mVp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        topBarContainer.visibility = View.VISIBLE
                        if (mBannerAd != null) {
                            bannerContainer.visibility = View.VISIBLE
                        } else {
                            bannerContainer.visibility = View.GONE
                        }
                        ivRight.setImageResource(R.drawable.ic_setting)
//                        mTvTitle.text = getString(R.string.app_name)
                    }
                    1 -> {
                        ivRight.setImageResource(R.drawable.ic_setting)
                        topBarContainer.visibility = View.GONE
//                        mTvTitle.text = getString(R.string.tab_guide)
                    }
                    2 -> {
                        ivRight.setImageResource(R.drawable.ic_backup)
                        topBarContainer.visibility = View.VISIBLE
                        bannerContainer.visibility = View.GONE
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }


    private fun initTab() {
        //tab
        mVp.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(mTab))
        mTab.setOnTabSelectedListener(this)

        val tabCount = TabModel.tabCount
        for (i in 0 until tabCount) {
            val tabModel = TabModel.getTab(i)
            val labelId = tabModel.labelResId
            val tab = mTab.newTab()
                .setTag(tabModel)
                .setCustomView(getTabView(i))
                .setContentDescription(labelId)
            val drawable = tabModel.drawable
            if (drawable != null) {
                tab.icon = drawable
            } else {
                tab.setIcon(tabModel.iconResId)
            }

            tab.setText(labelId)
            val imageView = tab.customView?.findViewById<ImageView>(R.id.icon)
            imageView?.setColorFilter(mainTabUnSelectColor, PorterDuff.Mode.SRC_IN)
            //解决首次tab文字颜色异常
            val textView = tab.customView?.findViewById<TextView>(R.id.text1)
            textView?.setTextColor(mainTabUnSelectColor)
            mTab.addTab(tab)
        }

        mTab.setSelectedTabIndicatorWidth(DisplayUtils.dip2px(20))
        mTab.setSelectedTabIndicatorHeight(DisplayUtils.dip2px(2))
        mTab.setSelectedTabIndicatorColor(mainTabHighlight)
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        mVp.currentItem = tab.position

        TabModel.selectedTab = (tab.tag as TabModel.Tab)
        for (i in 0 until mTab.tabCount) {
            val aTab = mTab.getTabAt(i)
            if (aTab != null) {
                val imageView = aTab.customView?.findViewById<ImageView>(R.id.icon)
                val textView = aTab.customView?.findViewById<TextView>(R.id.text1)
                if (aTab === tab) {
                    imageView?.setColorFilter(mainTabHighlight, PorterDuff.Mode.SRC_IN)
                    textView?.setTextColor(mainTabHighlight)
                } else {
                    imageView?.setColorFilter(mainTabUnSelectColor, PorterDuff.Mode.SRC_IN)
                    textView?.setTextColor(mainTabUnSelectColor)
                }
            }
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {}
    override fun onTabReselected(tab: TabLayout.Tab) {}

    private fun getTabView(position: Int): View {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_bottom_tab, null)
        val imageView = view.findViewById<ImageView>(R.id.icon)
        val textView = view.findViewById<TextView>(R.id.text1)
        val tab = TabModel.getTab(position)
        textView.setText(tab.labelResId)
        imageView.setImageResource(tab.iconResId)
        return view
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivRight -> {
                if (mVp.currentItem == 0) {
                    ActivityCollector.startActivity(this, SettingActivity::class.java)
                } else {
                    ActivityCollector.startActivity(this, BackupRestoreActivity::class.java)
                }

            }
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val currentFragment = mFragmentList[mVp.currentItem] as? BaseFragment
        if (currentFragment?.onKeyDown(keyCode, event) == true) {
            return true
        }

        return super.onKeyDown(keyCode, event)
    }


    override fun onDestroy() {
        super.onDestroy()
        DownloadManager.getInstance().cancelAllTask()
        mBannerAd?.destroy()
        mExitInsertAd?.destroy()
        ImageLoader.clearMemoryCache()
        mShakeAnimator?.cancel()
    }


    private fun loadBanner() {
        HandlerHelper.mainHandler.postDelayed({
            val container = findViewById<ViewGroup>(R.id.bannerContainer)
            AdChainHelper.loadAd(AdConstants.AD_NAME_BANNER, container, object : AdChainListener {
                override fun onLoaded(ad: IAd?) {
                    mBannerAd = ad
                    if (mVp.currentItem == 0) {
                        bannerContainer.visibility = View.VISIBLE
                    } else {
                        bannerContainer.visibility = View.GONE
                    }
                }

                override fun onFailed(msg: String) {}
                override fun onShowed() {}
                override fun onDismiss() {}

            })
        }, 3000)
    }

    private var mIsAdLoaded = false
    private fun loadExitInsert() {
        AdChainHelper.loadAd(AdConstants.AD_NAME_EXIT_INSERT, null, object : AdChainListener {
            override fun onLoaded(ad: IAd?) {
                mExitInsertAd = ad
                mIsAdLoaded = true
            }

            override fun onFailed(msg: String) {}
            override fun onShowed() {
                mIsAdLoaded = false
            }

            override fun onDismiss() {}

        })
    }

    override fun onResume() {
        super.onResume()
        mBannerAd?.onAdResume()
    }

    override fun onPause() {
        super.onPause()
        mBannerAd?.onAdPause()
    }

    override fun onBackPressed() {
        if (mIsAdLoaded) {
            mExitInsertAd?.show()
            mIsAdLoaded = false
        } else {
            if (UMeng.getChannel() == "google") {
                //谷歌渠道，首次评分，其余推荐
                if (mIsShowComment) {
                    if (RecommendGlobal.recommendData.isEmpty()) {
                        showComment()
                    } else {
                        showRecommendDialog()
                    }
                } else {
                    showComment()
                }
            } else {
                //其他渠道推荐
                if (RecommendGlobal.recommendData.isEmpty()) {
                    checkExit()
                } else {
                    showRecommendDialog()
                }
            }
        }
    }

    private fun showRecommendDialog() {
        val dialog =
            RecommendDialogHelper.createRecommendDialog(this, object : RecommendDialogListener {
                override fun onMore(dialog: Dialog?) {
                    dialog?.dismiss()
                }

                override fun onReject(dialog: Dialog?) {
                    dialog?.dismiss()
                    GlobalScope.launch {
                        delay(200)
                        finish()
                    }
                }

                override fun onBackPress(dialog: Dialog?) {
                    dialog?.dismiss()
                    GlobalScope.launch {
                        delay(200)
                        finish()
                    }
                }
            })
        RecommendDialogHelper.show(this, dialog)
    }

    private var mIsShowComment = false
    private fun showComment() {
        val dialog = CommentHelper.createCommentDialog(this, object : CommentListener {
            override fun onComment(dialog: Dialog?) {
                dialog?.dismiss()
            }

            override fun onReject(dialog: Dialog?) {
                dialog?.dismiss()
                GlobalScope.launch {
                    delay(200)
                    finish()
                }
            }

            override fun onBackPress(dialog: Dialog?) {
                dialog?.dismiss()
                GlobalScope.launch {
                    delay(200)
                    finish()
                }
            }
        })

        CommentHelper.show(this, dialog)
        mIsShowComment = true
    }


}