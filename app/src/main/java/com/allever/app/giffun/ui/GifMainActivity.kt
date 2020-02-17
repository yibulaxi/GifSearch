package com.allever.app.giffun.ui

import android.Manifest
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.allever.app.giffun.R
import com.allever.app.giffun.ad.AdConstants
import com.allever.app.giffun.app.Global
import com.allever.app.giffun.bean.DataBean
import com.allever.app.giffun.bean.TrendingResponse
import com.allever.app.giffun.function.download.DownloadManager
import com.allever.app.giffun.ui.adapter.GifAdapter
import com.allever.app.giffun.ui.mvp.model.RetrofitUtil
import com.allever.app.giffun.ui.widget.RecyclerViewScrollListener
import com.allever.app.giffun.util.ImageLoader
import com.allever.app.giffun.util.SpUtils
import com.allever.lib.ad.chain.AdChainHelper
import com.allever.lib.ad.chain.AdChainListener
import com.allever.lib.ad.chain.IAd
import com.allever.lib.comment.CommentHelper
import com.allever.lib.comment.CommentListener
import com.allever.lib.common.app.BaseActivity
import com.allever.lib.common.util.ActivityCollector
import com.allever.lib.common.util.log
import com.allever.lib.common.util.toast
import com.allever.lib.permission.PermissionCompat
import com.allever.lib.permission.PermissionListener
import com.allever.lib.permission.PermissionManager
import com.allever.lib.recommend.*
import com.allever.lib.umeng.UMeng
import kotlinx.android.synthetic.main.activity_gif_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rx.Subscriber

class GifMainActivity : BaseActivity(), View.OnClickListener {


    private var mAdapter: GifAdapter? = null
    private var mGifDataList = mutableListOf<DataBean>()
    private lateinit var mProgressDialog: ProgressDialog
    private lateinit var recyclerViewScrollListener: RecyclerViewScrollListener

    private var mBannerAd: IAd? = null
    private var mExitInsertAd: IAd? = null
    private var mDetailInsertAd: IAd? = null
    private var mIsDetailAdShowed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gif_main)

        ivSetting.setOnClickListener(this)
        ivRecommend.setOnClickListener(this)

        mProgressDialog = ProgressDialog(this)

        ivRetry?.setOnClickListener {
            requestPermission()
            ivRetry?.visibility = View.GONE
        }

        mAdapter = GifAdapter(this, R.layout.item_gif, mGifDataList)

        recyclerViewScrollListener = RecyclerViewScrollListener(object :
            RecyclerViewScrollListener.OnRecycleRefreshListener {
            override fun refresh() {

            }

            override fun loadMore() {
                getData(true)
            }
        })


        gifRecyclerView.addOnScrollListener(recyclerViewScrollListener)

        gifRecyclerView.layoutManager = LinearLayoutManager(this)
        val pagerSnapHelper = object : PagerSnapHelper() {
            override fun findTargetSnapPosition(
                layoutManager: RecyclerView.LayoutManager,
                velocityX: Int,
                velocityY: Int
            ): Int {
                val position = super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
                log("当前页： $position")
                gifRecyclerView.findViewHolderForLayoutPosition(position)
                return position
            }
        }
        pagerSnapHelper.attachToRecyclerView(gifRecyclerView)
        gifRecyclerView.adapter = mAdapter

        requestPermission()

        loadBanner()
        loadExitInsert()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivSetting -> {
                ActivityCollector.startActivity(this, SettingActivity::class.java)
            }
            R.id.ivRecommend -> {
                RecommendActivity.start(this, UMeng.getChannel())
            }
        }
    }

    private fun requestPermission() {
        if (!PermissionManager.hasPermissions(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            //弹窗
            AlertDialog.Builder(this)
                .setMessage(R.string.permission_tips)
                .setPositiveButton(R.string.permission_accept) { dialog, which ->
                    dialog.dismiss()
                    PermissionManager.request(
                        object : PermissionListener {
                            override fun onGranted(grantedList: MutableList<String>) {
                                Global.createDir()
                                getData()
                            }

                            override fun onDenied(deniedList: MutableList<String>) {
                                super.onDenied(deniedList)
                                toast(R.string.reject_permission_tips)
                                ivRetry?.visibility = View.VISIBLE
                            }

                            override fun alwaysDenied(deniedList: MutableList<String>) {
                                super.alwaysDenied(deniedList)
                                if (
                                    PermissionCompat.hasPermission(this@GifMainActivity,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.READ_PHONE_STATE)
                                ) {
                                    Global.createDir()
                                    getData()
                                } else {
                                    PermissionManager.jumpPermissionSetting(this@GifMainActivity, 1001,
                                        DialogInterface.OnClickListener { dialog, which ->
                                            toast(R.string.reject_permission_tips)
                                            ivRetry?.visibility = View.VISIBLE
                                        })
                                }

                            }

                        }, Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
                .setNegativeButton(R.string.permission_reject) { dialog, which ->
                    dialog.dismiss()
                    toast(R.string.reject_permission_tips)
                    ivRetry?.visibility = View.VISIBLE
                }
                .create()
                .show()
        } else {
            Global.createDir()
            getData()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            if (!PermissionManager.hasPermissions(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                ivRetry?.visibility = View.VISIBLE
                gifRecyclerView?.visibility = View.GONE
            }
        }
    }

    private fun getData(isLoadMore: Boolean = false) {
        val count = 10
        var offset = SpUtils.getString(Global.SP_OFFSET, "0")
        log("offset = $offset")
        showLoadingProgressDialog(getString(R.string.loading))
        RetrofitUtil.trendingGif(offset.toInt(), count, object : Subscriber<TrendingResponse>() {
            override fun onCompleted() {}
            override fun onError(e: Throwable) {
                e.printStackTrace()
                log("请求失败")
                hideLoadingProgressDialog()
                recyclerViewScrollListener.setLoadDataStatus(false)
                if (!isLoadMore) {
                    gifRecyclerView?.visibility = View.GONE
                    ivRetry?.visibility = View.VISIBLE
                }
            }

            override fun onNext(bean: TrendingResponse) {
                hideLoadingProgressDialog()

                if (mDetailInsertAd != null) {
                    mHandler.postDelayed({
                        toast(R.string.loading_ad_tips)
                        mDetailInsertAd?.show()
                        mIsDetailAdShowed = true
                    }, 200)
                }

                mHandler.postDelayed({
                    recyclerViewScrollListener.setLoadDataStatus(false)
                    gifRecyclerView?.visibility = View.VISIBLE

                    log("请求成功")
                    val data = bean.data
                    data?.map {
                        log("trending = ${it.images.original.url}")
                    }

                    if (!isLoadMore) {
                        mGifDataList.clear()
                    }

                    mGifDataList.addAll(data)

                    mAdapter?.notifyDataSetChanged()

                    offset = if (mGifDataList.size < count) {
                        "0"
                    } else {
                        (offset.toInt() + count + 1).toString()
                    }
                    SpUtils.putString(Global.SP_OFFSET, offset)

                    loadDetailInsert()
                }, 500)

            }
        })
    }

    private fun showLoadingProgressDialog(msg: String) {
        if (!mProgressDialog.isShowing) {
            mProgressDialog.setMessage(msg)
            mProgressDialog.show()
        }
    }

    private fun hideLoadingProgressDialog() {
        if (mProgressDialog.isShowing) {
            mProgressDialog.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DownloadManager.getInstance().cancelAllTask()
        mBannerAd?.destroy()
        mExitInsertAd?.destroy()
        mDetailInsertAd?.destroy()
        ImageLoader.clearMemoryCache()
    }

    private fun loadBanner() {
        mHandler.postDelayed({
            val container = findViewById<ViewGroup>(R.id.bannerContainer)
            AdChainHelper.loadAd(AdConstants.AD_NAME_BANNER, container, object : AdChainListener {
                override fun onLoaded(ad: IAd?) {
                    mBannerAd = ad
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

    private fun loadDetailInsert() {
        AdChainHelper.loadAd(AdConstants.AD_NAME_DETAIL_INSERT, null, object : AdChainListener {
            override fun onLoaded(ad: IAd?) {
                mDetailInsertAd = ad
            }

            override fun onFailed(msg: String) {}
            override fun onShowed() {
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
        if (mIsAdLoaded && !mIsDetailAdShowed) {
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
        val dialog = RecommendDialogHelper.createRecommendDialog(this, object : RecommendDialogListener {
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