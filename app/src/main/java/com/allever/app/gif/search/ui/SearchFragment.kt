package com.allever.app.gif.search.ui

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.allever.app.gif.search.BuildConfig
import com.allever.app.gif.search.R
import com.allever.app.gif.search.ad.AdConstants
import com.allever.app.gif.search.app.BaseFragment
import com.allever.app.gif.search.app.Global
import com.allever.app.gif.search.bean.DataBean
import com.allever.app.gif.search.bean.SearchResponse
import com.allever.app.gif.search.bean.event.DownloadFinishEvent
import com.allever.app.gif.search.bean.event.LikeEvent
import com.allever.app.gif.search.bean.event.RemoveLikeListEvent
import com.allever.app.gif.search.function.download.DownloadManager
import com.allever.app.gif.search.ui.adapter.GifAdapter
import com.allever.app.gif.search.ui.adapter.bean.GifItem
import com.allever.app.gif.search.ui.mvp.model.RetrofitUtil
import com.allever.app.gif.search.ui.mvp.presenter.SearchPresenter
import com.allever.app.gif.search.ui.mvp.view.ISearchView
import com.allever.app.gif.search.ui.widget.RecyclerViewScrollListener
import com.allever.app.gif.search.util.ImageLoader
import com.allever.app.gif.search.util.SpUtils
import com.allever.lib.ad.chain.AdChainHelper
import com.allever.lib.ad.chain.AdChainListener
import com.allever.lib.ad.chain.IAd
import com.allever.lib.common.util.log
import com.allever.lib.common.util.toast
import com.allever.lib.ui.searchview.SearchView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rx.Subscriber

class SearchFragment : BaseFragment<ISearchView, SearchPresenter>(), ISearchView {

    private lateinit var mIvRetry: View
    private lateinit var mSearchView: SearchView

    private lateinit var mRv: RecyclerView
    private var mAdapter: GifAdapter? = null
    private var mGifDataList = mutableListOf<GifItem>()
    private lateinit var mProgressDialog: ProgressDialog
    private lateinit var recyclerViewScrollListener: RecyclerViewScrollListener

    private lateinit var mKeyword: String

    private var mDetailInsertAd: IAd? = null


    override fun getContentView(): Int = R.layout.fragment_search

    override fun initView(root: View) {

        EventBus.getDefault().register(this)

        mKeyword = arguments?.getString(EXTRA_KEY_WORD) ?: ""

        mProgressDialog = ProgressDialog(activity)

        mIvRetry = root.findViewById(R.id.ivRetry)
        mIvRetry.setOnClickListener {
            mIvRetry.visibility = View.GONE
            search(mKeyword)
        }

        mSearchView = root.findViewById(R.id.searchView)
        mSearchView.setText(mKeyword)
        mSearchView.setSelection(mKeyword.length)
        mSearchView.addSearchListener {
            search(it)
        }

        mAdapter = GifAdapter(context!!, R.layout.item_gif, mGifDataList)

        recyclerViewScrollListener = RecyclerViewScrollListener(object :
            RecyclerViewScrollListener.OnRecycleRefreshListener {
            override fun refresh() {

            }

            override fun loadMore() {
                showLoadingProgressDialog(getString(R.string.searching))
                search(mKeyword, true)
            }
        })


        mRv = root.findViewById(R.id.gifRecyclerView)
        mRv.addOnScrollListener(recyclerViewScrollListener)

        mRv.layoutManager = LinearLayoutManager(context)
        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(mRv)
        mRv.adapter = mAdapter

        if (mKeyword != "") {
            search(mKeyword)
        }
    }

    override fun initData() {
    }


    override fun createPresenter(): SearchPresenter = SearchPresenter()

    private fun search(keyword: String, isLoadMore: Boolean = false) {
        if (keyword == "") {
            toast(getString(R.string.please_input_search_content))
            return
        }

        var keyword = keyword
        if (!BuildConfig.DEBUG) {
            keyword = keyword.replace("sexy", "")
            keyword = keyword.replace("sex", "")
        }

        mKeyword = keyword
        val count = Global.SHOW_COUNT
        var offset = SpUtils.getString(Global.SP_SEARCH_OFFSET, "0")
        log("offset = $offset")
        showLoadingProgressDialog(getString(R.string.searching))
        RetrofitUtil.searchGif(
            keyword,
            offset.toInt(),
            count,
            object : Subscriber<SearchResponse>() {
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

                override fun onNext(bean: SearchResponse) {
                    hideLoadingProgressDialog()

                    if (mDetailInsertAd != null) {
                        mHandler.postDelayed({
                            toast(R.string.loading_ad_tips)
                            mDetailInsertAd?.show()
                        }, 200)
                    }

                    mHandler.postDelayed({
                        recyclerViewScrollListener.setLoadDataStatus(false)
                        mRv.visibility = View.VISIBLE

                        log("搜索成功")
                        val data = bean.data
                        val gifItemList = mutableListOf<GifItem>()
                        data?.map {
                            log("trending = ${it.images.original.url}")
                            val gifItem = GifItem()
                            gifItem.id = it.id
                            gifItem.type = 1
                            gifItem.avatar = it?.user?.avatar_url?:""
                            gifItem.nickname = it?.user?.display_name?:""
                            gifItem.title = it.title?:""
                            gifItem.size = it.images.fixed_height.size.toLong()
                            gifItem.url = it.images.fixed_height.url
                            gifItemList.add(gifItem)
                        }

                        if (!isLoadMore) {
                            mGifDataList.clear()
                        }

                        mGifDataList.addAll(gifItemList)

                        mAdapter?.notifyDataSetChanged()

                        offset = if (mGifDataList.size < count) {
                            "0"
                        } else {
                            (offset.toInt() + count + 1).toString()
                        }
                        SpUtils.putString(Global.SP_SEARCH_OFFSET, offset)

                        loadDetailInsert()
                    }, 500)


                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        DownloadManager.getInstance().cancelAllTask()
        ImageLoader.clearMemoryCache()
        mDetailInsertAd?.destroy()
        EventBus.getDefault().unregister(this)

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
            mAdapter?.notifyItemChanged(position, position)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownloadFinishEvent(event: DownloadFinishEvent) {
        if (!userVisibleHint) {
            val position = Global.getIndex(event.id, mGifDataList)
            mAdapter?.notifyItemChanged(position, position)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRemoveLikeLietEvent(event: RemoveLikeListEvent) {
        if (!userVisibleHint) {
            event.gifIdList.map {
                val position = Global.getIndex(it, mGifDataList)
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