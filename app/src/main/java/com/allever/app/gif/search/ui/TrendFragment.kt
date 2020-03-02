package com.allever.app.gif.search.ui

import android.Manifest
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.allever.app.gif.search.R
import com.allever.app.gif.search.ad.AdConstants
import com.allever.app.gif.search.app.BaseFragment
import com.allever.app.gif.search.app.Global
import com.allever.app.gif.search.bean.DataBean
import com.allever.app.gif.search.bean.TrendingResponse
import com.allever.app.gif.search.bean.event.DownloadFinishEvent
import com.allever.app.gif.search.bean.event.LikeEvent
import com.allever.app.gif.search.bean.event.RemoveLikeListEvent
import com.allever.app.gif.search.ui.adapter.GifAdapter
import com.allever.app.gif.search.ui.mvp.model.RetrofitUtil
import com.allever.app.gif.search.ui.mvp.presenter.TrendPresenter
import com.allever.app.gif.search.ui.mvp.view.TrendView
import com.allever.app.gif.search.ui.widget.RecyclerViewScrollListener
import com.allever.app.gif.search.util.SpUtils
import com.allever.lib.ad.chain.AdChainHelper
import com.allever.lib.ad.chain.AdChainListener
import com.allever.lib.ad.chain.IAd
import com.allever.lib.common.util.log
import com.allever.lib.common.util.toast
import com.allever.lib.permission.PermissionCompat
import com.allever.lib.permission.PermissionListener
import com.allever.lib.permission.PermissionManager
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rx.Subscriber

class TrendFragment : BaseFragment<TrendView, TrendPresenter>(), TrendView {

    private lateinit var mIvRetry: View
    private lateinit var mRv: RecyclerView
    private lateinit var mAdapter: GifAdapter

    private var mGifDataList = mutableListOf<DataBean>()
    private lateinit var mProgressDialog: ProgressDialog
    private lateinit var recyclerViewScrollListener: RecyclerViewScrollListener


    private var mDetailInsertAd: IAd? = null
    private var mIsDetailAdShowed = false

    override fun getContentView(): Int = R.layout.fragment_trend

    override fun initView(root: View) {
        EventBus.getDefault().register(this)
        mProgressDialog = ProgressDialog(activity)

        mRv = root.findViewById(R.id.gifRecyclerView)
        mIvRetry = root.findViewById<View>(R.id.ivRetry)
        mIvRetry.setOnClickListener {
            requestPermission()
            mIvRetry.visibility = View.GONE
        }

        mAdapter = GifAdapter(context!!, R.layout.item_gif, mGifDataList)

        recyclerViewScrollListener = RecyclerViewScrollListener(object :
            RecyclerViewScrollListener.OnRecycleRefreshListener {
            override fun refresh() {

            }

            override fun loadMore() {
                getData(true)
            }
        })


        mRv.addOnScrollListener(recyclerViewScrollListener)

        mRv.layoutManager = LinearLayoutManager(context)
        val pagerSnapHelper = object : PagerSnapHelper() {
            override fun findTargetSnapPosition(
                layoutManager: RecyclerView.LayoutManager,
                velocityX: Int,
                velocityY: Int
            ): Int {
                val position = super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
                log("当前页： $position")
                mRv.findViewHolderForLayoutPosition(position)
                return position
            }
        }
        pagerSnapHelper.attachToRecyclerView(mRv)
        mRv.adapter = mAdapter

        requestPermission()
    }

    override fun initData() {
    }

    override fun createPresenter() = TrendPresenter()

    override fun onDestroyView() {
        super.onDestroyView()
        mDetailInsertAd?.destroy()
        EventBus.getDefault().unregister(this)
    }

    private fun requestPermission() {
        if (!PermissionManager.hasPermissions(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            //弹窗
            AlertDialog.Builder(activity!!)
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
                                mIvRetry.visibility = View.VISIBLE
                            }

                            override fun alwaysDenied(deniedList: MutableList<String>) {
                                super.alwaysDenied(deniedList)
                                if (
                                    PermissionCompat.hasPermission(
                                        activity!!,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.READ_PHONE_STATE
                                    )
                                ) {
                                    Global.createDir()
                                    getData()
                                } else {
                                    PermissionManager.jumpPermissionSetting(activity, 1001,
                                        DialogInterface.OnClickListener { dialog, which ->
                                            toast(R.string.reject_permission_tips)
                                            mIvRetry.visibility = View.VISIBLE
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
                    mIvRetry.visibility = View.VISIBLE
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
                mIvRetry.visibility = View.VISIBLE
                mRv.visibility = View.GONE
            }
        }
    }

    private fun getData(isLoadMore: Boolean = false) {
        val count = Global.SHOW_COUNT
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
                    mRv.visibility = View.GONE
                    mIvRetry.visibility = View.VISIBLE
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
                    mRv.visibility = View.VISIBLE

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLikeUpdate(likeEvent: LikeEvent) {
        if (!userVisibleHint) {
            val position = Global.getIndex(likeEvent.id, mGifDataList)
            mAdapter.notifyItemChanged(position, position)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownloadFinishEvent(event: DownloadFinishEvent) {
        if (!userVisibleHint) {
            val position = Global.getIndex(event.id, mGifDataList)
            mAdapter.notifyItemChanged(position, position)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRemoveLikeLietEvent(event: RemoveLikeListEvent) {
        if (!userVisibleHint) {
            event.gifIdList.map {
                val position = Global.getIndex(it, mGifDataList)
                mAdapter.notifyItemChanged(position, position)
            }
        }
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

}