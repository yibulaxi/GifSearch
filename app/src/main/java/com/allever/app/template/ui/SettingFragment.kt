package com.allever.app.template.ui

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.allever.app.template.R
import com.allever.app.template.app.BaseFragment
import com.allever.app.template.ui.mvp.presenter.SettingPresenter
import com.allever.app.template.ui.mvp.view.SettingView
import com.allever.app.template.util.SystemUtils
import com.allever.lib.common.app.App
import com.allever.lib.common.util.FeedbackHelper
import com.allever.lib.common.util.toast
import com.allever.lib.permission.PermissionUtil

class SettingFragment : BaseFragment<SettingView, SettingPresenter>(), SettingView,
    View.OnClickListener {

    private lateinit var mBannerContainer: ViewGroup
//    private  var mAdWorker: IAdWorker? = null
//    private var mVideoAdWorker: IRewardVideoAdWorker? = null

    override fun getContentView(): Int = R.layout.fragment_setting

    override fun initView(root: View) {
        root.findViewById<View>(R.id.setting_tv_share).setOnClickListener(this)
        root.findViewById<TextView>(R.id.setting_tv_feedback).setOnClickListener(this)
        root.findViewById<TextView>(R.id.setting_tv_about).setOnClickListener(this)
        root.findViewById<TextView>(R.id.setting_tv_permission).setOnClickListener(this)
        root.findViewById<TextView>(R.id.setting_tv_support).setOnClickListener(this)

//        mBannerContainer = root.findViewById(R.id.banner_container)
//        mAdWorker = AdHelper.loadAndShowBanner(AdContract.SETTING_BANNER, mBannerContainer, null)
    }

    override fun initData() {
    }

    override fun createPresenter(): SettingPresenter = SettingPresenter()

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.setting_tv_permission -> {
                PermissionUtil.GoToSetting(activity)
            }
            R.id.setting_tv_share -> {
                val msg = "【${getString(R.string.app_name)}】这个应用很不错，推荐一下。获取地址 \nhttp://app.mi.com/details?id=com.allever.app.virtual.call"
                val intent = SystemUtils.getShareIntent(App.context, msg)
                startActivity(intent)
            }
            R.id.setting_tv_feedback -> {
                FeedbackHelper.feedback(activity)
            }
            R.id.setting_tv_about -> {
                AboutActivity.start(activity!!)
            }
            R.id.setting_tv_support -> {
                supportUs()
            }
        }
    }

    override fun onDestroy() {
//        AdHelper.destroy(mAdWorker)
//        AdHelper.destroyVideo(mVideoAdWorker)
        super.onDestroy()
    }

    private fun supportUs() {
//        AlertDialog.Builder(activity!!)
//            .setTitle("温馨提示")
//            .setMessage("该操作会消耗一定的数据流量，您要观看吗?")
//            .setPositiveButton("立即观看") { dialog, which ->
//                dialog.dismiss()
//                //todo 加载视频
//                AdHelper.destroyVideo(mVideoAdWorker)
//                mVideoAdWorker = AdHelper.loadRewardVideo(AdContract.SUPPORT_ENCOURAGE, object : AdListener {
//                    override fun onLoaded() {
//                        AdHelper.showVideo(mVideoAdWorker)
//                    }
//                    override fun onShowed() {}
//                    override fun onDismiss() {}
//                    override fun onFailed() {
//                        //失败时显示插屏
//                        toast("获取视频失败, 您可以点击小广告，也是对我们的一种支持.")
//                    }
//                })
//            }
//            .setNegativeButton("残忍拒绝") { dialog, which ->
//                dialog.dismiss()
//                toast("您可以点击小广告，也是对我们的一种支持.")
//            }
//            .create()
//            .show()
    }
}