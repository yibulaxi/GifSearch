package com.allever.app.gif.search.ui.search

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.allever.app.gif.search.BR
import com.allever.app.gif.search.R
import com.allever.app.gif.search.ad.AdConstants
import com.allever.app.gif.search.ad.SimpleAdChainListener
import com.allever.app.gif.search.app.BaseFragment2
import com.allever.app.gif.search.app.Global
import com.allever.app.gif.search.bean.event.DownloadFinishEvent
import com.allever.app.gif.search.bean.event.LikeEvent
import com.allever.app.gif.search.bean.event.RemoveLikeListEvent
import com.allever.app.gif.search.databinding.FragmentSearchBinding
import com.allever.app.gif.search.function.download.DownloadManager
import com.allever.app.gif.search.function.store.Repository
import com.allever.app.gif.search.ui.adapter.GifAdapter
import com.allever.app.gif.search.ui.mvp.view.ISearchView
import com.allever.app.gif.search.ui.search.model.SearchViewModel
import com.allever.app.gif.search.ui.widget.RecyclerViewScrollListener
import com.allever.app.gif.search.util.ImageLoader
import com.allever.app.gif.search.util.SpUtils
import com.allever.lib.ad.chain.AdChainHelper
import com.allever.lib.ad.chain.IAd
import com.allever.lib.common.util.log
import com.allever.lib.common.util.toast
import com.xm.lib.base.config.DataBindingConfig
import com.xm.lib.util.HandlerHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SearchFragment : BaseFragment2<FragmentSearchBinding, SearchViewModel>(), ISearchView {

    private var mAdapter: GifAdapter? = null
    private lateinit var mProgressDialog: ProgressDialog
    private lateinit var recyclerViewScrollListener: RecyclerViewScrollListener

    private lateinit var mKeyword: String

    private var mDetailInsertAd: IAd? = null

    override fun initDataBindingConfig() = DataBindingConfig(R.layout.fragment_search, BR.searchViewModel)

    override fun initDataAndEvent() {
        EventBus.getDefault().register(this)

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

        mAdapter = GifAdapter(context!!, R.layout.item_gif, mViewModel.gifDataList)

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
        ImageLoader.clearMemoryCache()
        mDetailInsertAd?.destroy()
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
        val count = Global.SHOW_COUNT
        var offset = SpUtils.getString(Global.SP_SEARCH_OFFSET, "0")
        log("offset = $offset")
        showLoadingProgressDialog(getString(R.string.searching))
        mViewModel.viewModelScope.launch(Dispatchers.Main) {
            val gifItemList = Repository.search(keyword, offset)
            hideLoadingProgressDialog()
            if (mDetailInsertAd != null) {
                HandlerHelper.mainHandler.postDelayed({
                    toast(R.string.loading_ad_tips)
                    mDetailInsertAd?.show()
                }, 200)
            }

            recyclerViewScrollListener.setLoadDataStatus(false)
            mBinding.gifRecyclerView.visibility = View.VISIBLE

            if (!isLoadMore) {
                mViewModel.gifDataList.clear()
            }

            mViewModel.gifDataList.addAll(gifItemList)

            mAdapter?.notifyDataSetChanged()

            offset = if (mViewModel.gifDataList.size < count) {
                "0"
            } else {
                (offset.toInt() + count + 1).toString()
            }


            SpUtils.putString(Global.SP_SEARCH_OFFSET, offset)

            loadDetailInsert()

            if (gifItemList.isEmpty()) {
                recyclerViewScrollListener.setLoadDataStatus(false)
                if (!isLoadMore) {
                    mBinding.gifRecyclerView.visibility = View.GONE
                    mBinding.ivRetry.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun loadDetailInsert() {
        AdChainHelper.loadAd(AdConstants.AD_NAME_DETAIL_INSERT, null, object : SimpleAdChainListener {
            override fun onLoaded(ad: IAd?) {
                mDetailInsertAd = ad
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
            val position = Global.getIndex(likeEvent.id, mViewModel.gifDataList)
            mAdapter?.notifyItemChanged(position, position)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownloadFinishEvent(event: DownloadFinishEvent) {
        if (!userVisibleHint) {
            val position = Global.getIndex(event.id, mViewModel.gifDataList)
            mAdapter?.notifyItemChanged(position, position)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRemoveLikeLietEvent(event: RemoveLikeListEvent) {
        if (!userVisibleHint) {
            event.gifIdList.map {
                val position = Global.getIndex(it, mViewModel.gifDataList)
                mAdapter?.notifyItemChanged(position, position)
            }
        }
    }

    companion object {

        private const val EXTRA_KEY_WORD = "EXTRA_KEY_WORD"

        fun newInstance(keyword: String): SearchFragment {
            val fragment = SearchFragment()
            val bundle = Bundle()
            bundle.putString(EXTRA_KEY_WORD, keyword)
            fragment.arguments = bundle
            return fragment
        }
    }
}