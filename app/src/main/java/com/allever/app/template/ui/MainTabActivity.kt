package com.allever.app.template.ui

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.allever.app.template.R
import com.allever.app.template.app.BaseActivity
import com.allever.app.template.ui.adapter.ViewPagerAdapter
import com.allever.app.template.ui.mvp.presenter.MainPresenter
import com.allever.app.template.ui.mvp.view.MainView
import com.allever.app.template.util.DisplayUtils
import com.allever.lib.common.ui.widget.tab.TabLayout
import com.allever.lib.common.util.ActivityCollector

class MainTabActivity: BaseActivity<MainView, MainPresenter>(), MainView,
    TabLayout.OnTabSelectedListener, View.OnClickListener {
    private lateinit var mVp: ViewPager
    private lateinit var mViewPagerAdapter: ViewPagerAdapter
    private lateinit var mTab: TabLayout
    private lateinit var mTvTitle: TextView
    private var mainTabHighlight = 0
    private var mainTabUnSelectColor = 0

    private var mFragmentList = mutableListOf<Fragment>()


    override fun getContentView(): Any = R.layout.activity_main_tab

    override fun initView() {
        findViewById<View>(R.id.iv_right).setOnClickListener(this)
        mTab = findViewById(R.id.tab_layout)
        mVp = findViewById(R.id.id_main_vp)
        mTvTitle = findViewById(R.id.tv_label)

        mainTabHighlight = resources.getColor(R.color.main_tab_highlight)
        mainTabUnSelectColor = resources.getColor(R.color.main_tab_unselect_color)

        initViewPagerData()
        initViewPager()
        initTab()

    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.iv_right -> {
                ActivityCollector.startActivity(this, SettingActivity::class.java)
            }
        }
    }

    override fun initData() {
    }

    override fun createPresenter(): MainPresenter = MainPresenter()

    private fun initViewPagerData() {
        mFragmentList.add(MainFragment())
        mFragmentList.add(TwoFragment())
        mFragmentList.add(ThreeFragment())
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
                        mTvTitle.text = getString(R.string.app_name)
                    }
                    1 -> {
                        mTvTitle.text = "Two"
                    }
                    2 -> {
                        mTvTitle.text = getString(R.string.setting)
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

        mTab.setSelectedTabIndicatorWidth(DisplayUtils.dip2px(0))
        mTab.setSelectedTabIndicatorHeight(DisplayUtils.dip2px(0))
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

    override fun onBackPressed() {
        checkExit()
    }
}