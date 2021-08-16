package com.allever.app.gif.search.ui.main

import android.Manifest
import android.app.ProgressDialog
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.allever.app.gif.search.BR
import com.allever.app.gif.search.R
import com.allever.app.gif.search.ad.AdConstants
import com.allever.app.gif.search.app.BaseFragment2
import com.allever.app.gif.search.app.Global
import com.allever.app.gif.search.bean.event.DownloadFinishEvent
import com.allever.app.gif.search.bean.event.LikeEvent
import com.allever.app.gif.search.bean.event.RemoveLikeListEvent
import com.allever.app.gif.search.databinding.FragmentTrendBinding
import com.allever.app.gif.search.function.store.Repository
import com.allever.app.gif.search.function.store.Store
import com.allever.app.gif.search.function.store.Version
import com.allever.app.gif.search.ui.adapter.GifAdapter
import com.allever.app.gif.search.ui.main.model.TrendViewModel
import com.allever.app.gif.search.ui.mvp.view.TrendView
import com.allever.app.gif.search.ui.user.LoginActivity
import com.allever.app.gif.search.ui.widget.RecyclerViewScrollListener
import com.allever.app.gif.search.util.SpUtils
import com.allever.lib.ad.chain.AdChainHelper
import com.allever.lib.ad.chain.AdChainListener
import com.allever.lib.ad.chain.IAd
import com.allever.lib.common.util.log
import com.allever.lib.common.util.toast
import com.xm.lib.base.config.DataBindingConfig
import com.xm.lib.manager.IntentManager
import com.xm.lib.permission.PermissionCompat
import com.xm.lib.util.CoroutineHelper
import com.xm.lib.util.HandlerHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class TrendFragment : BaseFragment2<FragmentTrendBinding, TrendViewModel>(), TrendView {

    private lateinit var mProgressDialog: ProgressDialog
    private lateinit var recyclerViewScrollListener: RecyclerViewScrollListener

    private var mDetailInsertAd: IAd? = null
    private var mIsDetailAdShowed = false

    override fun initDataBindingConfig() = DataBindingConfig(R.layout.fragment_trend, BR.trendViewModel)

    override fun initDataAndEvent() {
        EventBus.getDefault().register(this)
        mProgressDialog = ProgressDialog(activity)

        mBinding.ivRetry.setOnClickListener {
            requestPermission()
            mBinding.ivRetry.visibility = View.GONE
        }

        mViewModel.adapter = GifAdapter(context!!, R.layout.item_gif, mViewModel.gifDataList)

        recyclerViewScrollListener = RecyclerViewScrollListener(object :
            RecyclerViewScrollListener.OnRecycleRefreshListener {
            override fun refresh() {

            }

            override fun loadMore() {
                getData(true)
            }
        })


        mBinding.gifRecyclerView.addOnScrollListener(recyclerViewScrollListener)

        mBinding.gifRecyclerView.layoutManager = LinearLayoutManager(context)
        val pagerSnapHelper = object : PagerSnapHelper() {
            override fun findTargetSnapPosition(
                layoutManager: RecyclerView.LayoutManager,
                velocityX: Int,
                velocityY: Int
            ): Int {
                val position = super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
                log("当前页： $position")
                mBinding.gifRecyclerView.findViewHolderForLayoutPosition(position)
                return position
            }
        }
        pagerSnapHelper.attachToRecyclerView(mBinding.gifRecyclerView)
        mBinding.gifRecyclerView.adapter = mViewModel.adapter

        requestPermission()
    }

    override fun destroyView() {
        mDetailInsertAd?.destroy()
        EventBus.getDefault().unregister(this)
    }

    private fun requestPermission() {

        PermissionCompat.with(this)
            .permission(Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .onExplain(getStringRes(R.string.permission_tips))
            .onSetting("手动设置权限")
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    Global.createDir()
                    getData()
                }
            }
    }

    private fun getData(isLoadMore: Boolean = false) {
        CoroutineHelper.mainCoroutine.launch {
            if (Store.getVersion() == Version.INTERNAL && !Global.checkLogin()) {
                mBinding.gifRecyclerView.visibility = View.GONE
                mBinding.ivRetry.visibility = View.VISIBLE
                IntentManager.startActivity(mCxt, LoginActivity::class.java)
                toast("未登录")
                return@launch
            }

            val count = Global.SHOW_COUNT
            var offset = SpUtils.getString(Global.SP_OFFSET, "0")
            log("offset = $offset")

            showLoadingProgressDialog(getString(R.string.loading))
            val gifItemList = Repository.getGifItemList(offset)
            hideLoadingProgressDialog()
            //成功
            if (mDetailInsertAd != null) {
                HandlerHelper.mainHandler.postDelayed({
                    toast(R.string.loading_ad_tips)
                    mDetailInsertAd?.show()
                    mIsDetailAdShowed = true
                }, 200)
            }

            delay(500)

            recyclerViewScrollListener.setLoadDataStatus(false)
            mBinding.gifRecyclerView.visibility = View.VISIBLE
            mBinding.ivRetry.visibility = View.GONE

            if (!isLoadMore) {
                mViewModel.gifDataList.clear()
            }

            mViewModel.gifDataList.addAll(gifItemList)

            mViewModel.adapter.notifyDataSetChanged()

            offset = if (Store.getVersion() == Version.INTERNATIONAL) {
                if (mViewModel.gifDataList.size < count) {
                    "0"
                } else {
                    (offset.toInt() + count + 1).toString()
                }
            } else {
                if (gifItemList.isEmpty()) {
                    "0"
                } else {
                    gifItemList.last().id
                }
            }

            SpUtils.putString(Global.SP_OFFSET, offset)

            loadDetailInsert()

            //失败
            if (gifItemList.isEmpty()) {
                recyclerViewScrollListener.setLoadDataStatus(false)
                if (!isLoadMore) {
                    mBinding.gifRecyclerView.visibility = View.GONE
                    mBinding.ivRetry.visibility = View.VISIBLE
                }
            }

        }
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
        val position = Global.getIndex(likeEvent.id, mViewModel.gifDataList)
        mViewModel.adapter.notifyItemChanged(position, position)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownloadFinishEvent(event: DownloadFinishEvent) {
        val position = Global.getIndex(event.id, mViewModel.gifDataList)
        mViewModel.adapter.notifyItemChanged(position, position)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRemoveLikeLietEvent(event: RemoveLikeListEvent) {
        event.gifIdList.map {
            val position = Global.getIndex(it, mViewModel.gifDataList)
            mViewModel.adapter.notifyItemChanged(position, position)
        }
    }

    private fun loadDetailInsert() {
        return
        AdChainHelper.loadAd(AdConstants.AD_NAME_DETAIL_INSERT, null, object : AdChainListener {
            override fun onLoaded(ad: IAd?) {
                mDetailInsertAd = ad
            }

            override fun onFailed(msg: String) {

            }

            override fun onShowed() {
            }

            override fun onDismiss() {
            }
        })
    }
}