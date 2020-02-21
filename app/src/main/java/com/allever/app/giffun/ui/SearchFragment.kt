package com.allever.app.giffun.ui

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.allever.app.giffun.R
import com.allever.app.giffun.ad.AdConstants
import com.allever.app.giffun.app.BaseFragment
import com.allever.app.giffun.app.Global
import com.allever.app.giffun.bean.DataBean
import com.allever.app.giffun.bean.SearchResponse
import com.allever.app.giffun.function.download.DownloadManager
import com.allever.app.giffun.ui.adapter.GifAdapter
import com.allever.app.giffun.ui.mvp.model.RetrofitUtil
import com.allever.app.giffun.ui.mvp.presenter.SearchPresenter
import com.allever.app.giffun.ui.mvp.view.ISearchView
import com.allever.app.giffun.ui.widget.RecyclerViewScrollListener
import com.allever.app.giffun.util.ImageLoader
import com.allever.app.giffun.util.SpUtils
import com.allever.lib.ad.chain.AdChainHelper
import com.allever.lib.ad.chain.AdChainListener
import com.allever.lib.ad.chain.IAd
import com.allever.lib.common.util.log
import com.allever.lib.common.util.toast
import com.allever.lib.ui.widget.SearchView
import rx.Subscriber

class SearchFragment : BaseFragment<ISearchView, SearchPresenter>(), ISearchView {

    private lateinit var mIvRetry: View
    private lateinit var mSearchView: SearchView

    private lateinit var mRv: RecyclerView
    private var mAdapter: GifAdapter? = null
    private var mGifDataList = mutableListOf<DataBean>()
    private lateinit var mProgressDialog: ProgressDialog
    private lateinit var recyclerViewScrollListener: RecyclerViewScrollListener

    private lateinit var mKeyword: String

    private var mDetailInsertAd: IAd? = null


    override fun getContentView(): Int = R.layout.fragment_search

    override fun initView(root: View) {

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
        mKeyword = keyword
        val count = 10
        var offset = SpUtils.getString(Global.SP_SEARCH_OFFSET, "0")
        log("offset = $offset")
        showLoadingProgressDialog(getString(R.string.searching))
        RetrofitUtil.searchGif(keyword, offset.toInt(), count, object : Subscriber<SearchResponse>() {
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