package com.allever.app.giffun.ui

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.allever.app.giffun.R
import com.allever.app.giffun.ad.AdConstants
import com.allever.app.giffun.app.Global
import com.allever.app.giffun.bean.DataBean
import com.allever.app.giffun.bean.SearchResponse
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
import com.allever.lib.common.app.BaseActivity
import com.allever.lib.common.util.log
import com.allever.lib.common.util.toast
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.activity_search.gifRecyclerView
import kotlinx.android.synthetic.main.activity_search.ivRetry
import rx.Subscriber

class SearchActivity: BaseActivity() {

    private var mAdapter: GifAdapter? = null
    private var mGifDataList = mutableListOf<DataBean>()
    private lateinit var mProgressDialog: ProgressDialog
    private lateinit var recyclerViewScrollListener: RecyclerViewScrollListener

    private lateinit var mKeyword: String

    private var mDetailInsertAd: IAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        mKeyword = intent?.getStringExtra(EXTRA_KEY_WORD) ?:""

        mProgressDialog = ProgressDialog(this)

        ivBack?.setOnClickListener {
            finish()
        }

        ivRetry?.setOnClickListener {
            ivRetry?.visibility = View.GONE
            search(mKeyword)
        }

        etSearch.setText(mKeyword)
        etSearch.setSelection(mKeyword.length)
        etSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val editable = etSearch.text
                if (editable != null) {
                    val content = editable.toString()
                    search(content)
                    return@OnEditorActionListener true
                }
                return@OnEditorActionListener false
            }
            false
        })

        mAdapter = GifAdapter(this, R.layout.item_gif, mGifDataList)

        recyclerViewScrollListener = RecyclerViewScrollListener(object :
            RecyclerViewScrollListener.OnRecycleRefreshListener {
            override fun refresh() {

            }

            override fun loadMore() {
                showLoadingProgressDialog(getString(R.string.searching))
                search(mKeyword, true)
            }
        })


        gifRecyclerView.addOnScrollListener(recyclerViewScrollListener)

        gifRecyclerView.layoutManager = LinearLayoutManager(this)
        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(gifRecyclerView)
        gifRecyclerView.adapter = mAdapter

        if (mKeyword != "") {
            search(mKeyword)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        mKeyword = intent?.getStringExtra(EXTRA_KEY_WORD) ?:""
        if (mKeyword != "") {
            mGifDataList.clear()
            mAdapter?.notifyDataSetChanged()
            etSearch?.setText(mKeyword)
            etSearch?.setSelection(mKeyword.length)
            search(mKeyword)
        }
    }

    private fun search(keyword: String, isLoadMore: Boolean = false) {
        if (keyword == "") {
            toast(getString(R.string.please_input_search_content))
            return
        }
        hideKeyboard()
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
                    gifRecyclerView?.visibility = View.GONE
                    ivRetry?.visibility = View.VISIBLE
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
                    gifRecyclerView?.visibility = View.VISIBLE

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

    override fun onDestroy() {
        super.onDestroy()
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

    fun hideKeyboard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager.isActive) {
            inputMethodManager.hideSoftInputFromWindow(
                currentFocus?.windowToken, 0
            )
        }
    }

    companion object {
        private const val EXTRA_KEY_WORD = "EXTRA_KEY_WORD"

        fun start(context: Context, keyword: String) {
            val intent = Intent(context, SearchActivity::class.java)
            intent.putExtra(EXTRA_KEY_WORD, keyword)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}