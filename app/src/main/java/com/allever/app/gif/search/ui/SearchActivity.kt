package com.allever.app.gif.search.ui

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.allever.app.gif.search.BuildConfig
import com.allever.app.gif.search.R
import com.allever.app.gif.search.ad.AdConstants
import com.allever.app.gif.search.app.Global
import com.allever.app.gif.search.bean.DataBean
import com.allever.app.gif.search.bean.SearchResponse
import com.allever.app.gif.search.function.download.DownloadManager
import com.allever.app.gif.search.ui.adapter.GifAdapter
import com.allever.app.gif.search.ui.adapter.bean.GifItem
import com.allever.app.gif.search.ui.mvp.model.RetrofitUtil
import com.allever.app.gif.search.ui.widget.RecyclerViewScrollListener
import com.allever.app.gif.search.util.ImageLoader
import com.allever.app.gif.search.util.SpUtils
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

class SearchActivity : BaseActivity() {

    private var mAdapter: GifAdapter? = null
    private var mGifDataList = mutableListOf<GifItem>()
    private lateinit var mProgressDialog: ProgressDialog
    private lateinit var recyclerViewScrollListener: RecyclerViewScrollListener

    private lateinit var mKeyword: String

    private var mDetailInsertAd: IAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        mKeyword = intent?.getStringExtra(EXTRA_KEY_WORD) ?: ""

        mProgressDialog = ProgressDialog(this)

        ivBack?.setOnClickListener {
            finish()
        }

        ivRetry?.setOnClickListener {
            ivRetry?.visibility = View.GONE
            search(mKeyword)
        }

        ivDelete?.setOnClickListener {
            etSearch?.setText("")
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

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 0) {
                    ivDelete?.visibility = View.GONE
                } else {
                    ivDelete?.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}


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
        mKeyword = intent?.getStringExtra(EXTRA_KEY_WORD) ?: ""
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

        var keyword = keyword
        if (!BuildConfig.DEBUG) {
            keyword = keyword.replace("sexy", "")
            keyword = keyword.replace("sex", "")
        }

        hideKeyboard()
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

    override fun onDestroy() {
        super.onDestroy()
//        DownloadManager.getInstance().cancelAllTask()
        val urls = mutableListOf<String>()
        mGifDataList.map {
            urls.add(it.url)
        }
        DownloadManager.getInstance().cancel(urls)
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