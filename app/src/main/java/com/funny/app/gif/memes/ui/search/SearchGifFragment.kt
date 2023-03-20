package com.funny.app.gif.memes.ui.search

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.funny.app.gif.memes.BR
import com.funny.app.gif.memes.R
//import com.allever.app.gif.search.ad.AdConstants
//import com.allever.app.gif.search.ad.SimpleAdChainListener
import com.funny.app.gif.memes.app.BaseAppFragment2
import com.funny.app.gif.memes.app.GlobalObj
import com.funny.app.gif.memes.bean.event.DownloadGifFinishEvent
import com.funny.app.gif.memes.bean.event.LikeGifEvent
import com.funny.app.gif.memes.bean.event.RemoveLikeGifListEvent
import com.funny.app.gif.memes.databinding.FragmentSearchBinding
import com.funny.app.gif.memes.function.download.DownloadManager
import com.funny.app.gif.memes.function.store.Repository
import com.funny.app.gif.memes.function.store.Store
import com.funny.app.gif.memes.function.store.Version
import com.funny.app.gif.memes.ui.ViewHelper
import com.funny.app.gif.memes.ui.adapter.GifItemAdapter
import com.funny.app.gif.memes.ui.mvp.view.ISearchView
import com.funny.app.gif.memes.ui.search.model.SearchViewModel
import com.funny.app.gif.memes.ui.widget.RecyclerViewScrollListener
import com.funny.app.gif.memes.util.ImgLoader
import com.funny.app.gif.memes.util.SpHelper


import com.funny.lib.common.util.log
import com.funny.lib.common.util.toast
import com.xm.lib.base.config.DataBindingConfig
import com.xm.lib.manager.statusbar.BarUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SearchGifFragment : BaseAppFragment2<FragmentSearchBinding, SearchViewModel>(), ISearchView {

    private var mAdapter: GifItemAdapter? = null
    private lateinit var mProgressDialog: ProgressDialog
    private lateinit var recyclerViewScrollListener: RecyclerViewScrollListener

    private lateinit var mKeyword: String

//    private var mDetailInsertAd: IAd? = null

    override fun initDataBindingConfig() = DataBindingConfig(R.layout.fragment_search, BR.searchViewModel)

    override fun initDataAndEvent() {
        EventBus.getDefault().register(this)

        ViewHelper.setMarginTop(mBinding.searchViewContainer, BarUtils.getStatusBarHeight())

        mKeyword = arguments?.getString(EXTRA_KEY_WORD) ?: ""

        mProgressDialog = ProgressDialog(activity)

        mBinding.ivRetry.setOnClickListener {
            mBinding.ivRetry.visibility = View.GONE
            search(mKeyword)
        }

        mBinding.searchView.setText(mKeyword)
        mBinding.searchView.setSelection(mKeyword.length)
        mBinding.searchView.addSearchListener {
            search(it)
        }

        mAdapter = GifItemAdapter(context!!, R.layout.item_gif, mViewModel.gifDataList)

        recyclerViewScrollListener = RecyclerViewScrollListener(object :
            RecyclerViewScrollListener.OnRecycleRefreshListener {
            override fun refresh() {

            }

            override fun loadMore() {
                showLoadingProgressDialog(getString(R.string.searching))
                search(mKeyword, true)
            }
        })


        mBinding.gifRecyclerView.addOnScrollListener(recyclerViewScrollListener)

        mBinding.gifRecyclerView.layoutManager = LinearLayoutManager(context)
        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(mBinding.gifRecyclerView)
        mBinding.gifRecyclerView.adapter = mAdapter

        if (mKeyword != "") {
            search(mKeyword)
        }
    }

    override fun destroyView() {
        DownloadManager.getInstance().cancelAllTask()
        ImgLoader.clearMemoryCache()
//        mDetailInsertAd?.destroy()
        EventBus.getDefault().unregister(this)
    }

    private fun search(keyword: String, isLoadMore: Boolean = false) {
        if (keyword == "") {
            toast(getString(R.string.please_input_search_content))
            return
        }

        val keyword = keyword
//        if (!BuildConfig.DEBUG) {
//            keyword = keyword.replace("sexy", "")
//            keyword = keyword.replace("sex", "")
//        }

        mKeyword = keyword
        val count = GlobalObj.SHOW_COUNT
        var offset = SpHelper.getString(GlobalObj.SP_SEARCH_OFFSET, "0")
        log("offset = $offset")
        showLoadingProgressDialog(getString(R.string.searching))
        mViewModel.viewModelScope.launch(Dispatchers.Main) {
            val gifItemList = Repository.search(keyword, offset)
            hideLoadingProgressDialog()
            mBinding.ivRetry.visibility = View.VISIBLE
//            if (mDetailInsertAd != null) {
//                HandlerHelper.mainHandler.postDelayed({
//                    toast(R.string.loading_ad_tips)
//                    mDetailInsertAd?.show()
//                }, 200)
//            }

            recyclerViewScrollListener.setLoadDataStatus(false)
            mBinding.gifRecyclerView.visibility = View.VISIBLE
            mBinding.ivRetry.visibility = View.GONE

            if (!isLoadMore) {
                mViewModel.gifDataList.clear()
            }

            mViewModel.gifDataList.addAll(gifItemList)

            mAdapter?.notifyDataSetChanged()

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

            SpHelper.putString(GlobalObj.SP_SEARCH_OFFSET, offset)

//            loadDetailInsert()

            if (gifItemList.isEmpty()) {
                recyclerViewScrollListener.setLoadDataStatus(false)
                if (!isLoadMore) {
                    mBinding.gifRecyclerView.visibility = View.GONE
                    mBinding.ivRetry.visibility = View.VISIBLE
                }
            }
        }
    }

//    private fun loadDetailInsert() {
//        AdChainHelper.loadAd(AdConstants.AD_NAME_DETAIL_INSERT, null, object : SimpleAdChainListener {
//            override fun onLoaded(ad: IAd?) {
//                mDetailInsertAd = ad
//            }
//        })
//    }

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
    fun onLikeUpdate(likeEvent: LikeGifEvent) {
        if (!userVisibleHint) {
            val position = GlobalObj.getIndex(likeEvent.id, mViewModel.gifDataList)
            mAdapter?.notifyItemChanged(position, position)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownloadFinishEvent(event: DownloadGifFinishEvent) {
        if (!userVisibleHint) {
            val position = GlobalObj.getIndex(event.id, mViewModel.gifDataList)
            mAdapter?.notifyItemChanged(position, position)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRemoveLikeLietEvent(event: RemoveLikeGifListEvent) {
        if (!userVisibleHint) {
            event.gifIdList.map {
                val position = GlobalObj.getIndex(it, mViewModel.gifDataList)
                mAdapter?.notifyItemChanged(position, position)
            }
        }
    }

    companion object {

        private const val EXTRA_KEY_WORD = "EXTRA_KEY_WORD"

        fun newInstance(keyword: String): SearchGifFragment {
            val fragment = SearchGifFragment()
            val bundle = Bundle()
            bundle.putString(EXTRA_KEY_WORD, keyword)
            fragment.arguments = bundle
            return fragment
        }
    }
}