package com.funny.app.gif.memes.ui.search

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.funny.app.gif.memes.BR
import com.funny.app.gif.memes.BuildConfig
import com.funny.app.gif.memes.R
//import com.allever.app.gif.search.ad.AdConstants
//import com.allever.app.gif.search.ad.SimpleAdChainListener
import com.funny.app.gif.memes.app.BaseDataActivity2
import com.funny.app.gif.memes.app.Global
import com.funny.app.gif.memes.databinding.ActivitySearchBinding
import com.funny.app.gif.memes.function.download.DownloadManager
import com.funny.app.gif.memes.function.store.Repository
import com.funny.app.gif.memes.function.store.Store
import com.funny.app.gif.memes.function.store.Version
import com.funny.app.gif.memes.ui.ViewHelper
import com.funny.app.gif.memes.ui.adapter.GifItemAdapter
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

class SearchActivity : BaseDataActivity2<ActivitySearchBinding, SearchViewModel>() {

    private var mAdapter: GifItemAdapter? = null
    private lateinit var mProgressDialog: ProgressDialog
    private lateinit var recyclerViewScrollListener: RecyclerViewScrollListener

    private lateinit var mKeyword: String

//    private var mDetailInsertAd: IAd? = null

    override fun isPaddingTop(): Boolean = false
    override fun statusColor(): Int = R.color.trans

    override fun initDataBindingConfig() =
        DataBindingConfig(R.layout.activity_search, BR.searchViewModel)

    override fun initDataAndEvent() {
        ViewHelper.setMarginTop(mBinding.topBar, BarUtils.getStatusBarHeight())
        mKeyword = intent?.getStringExtra(EXTRA_KEY_WORD) ?: ""

        mProgressDialog = ProgressDialog(this)

        mBinding.ivBack.setOnClickListener {
            finish()
        }

        mBinding.ivRetry.setOnClickListener {
            mBinding.ivRetry.visibility = View.GONE
            search(mKeyword)
        }

        mBinding.ivDelete.setOnClickListener {
            mBinding.etSearch.setText("")
        }

        mBinding.etSearch.setText(mKeyword)
        mBinding.etSearch.setSelection(mKeyword.length)
        mBinding.etSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val editable = mBinding.etSearch.text
                if (editable != null) {
                    val content = editable.toString()
                    search(content)
                    return@OnEditorActionListener true
                }
                return@OnEditorActionListener false
            }
            false
        })

        mBinding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 0) {
                    mBinding.ivDelete.visibility = View.GONE
                } else {
                    mBinding.ivDelete.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}


        })

        mAdapter = GifItemAdapter(this, R.layout.item_gif, mViewModel.gifDataList)

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

        mBinding.gifRecyclerView.layoutManager = LinearLayoutManager(this)
        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(mBinding.gifRecyclerView)
        mBinding.gifRecyclerView.adapter = mAdapter

        if (mKeyword != "") {
            search(mKeyword)
        }
    }

    override fun destroyView() {
        val urls = mutableListOf<String>()
        mViewModel.gifDataList.map {
            urls.add(it.url)
        }
        DownloadManager.getInstance().cancel(urls)
        ImgLoader.clearMemoryCache()
//        mDetailInsertAd?.destroy()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        mKeyword = intent?.getStringExtra(EXTRA_KEY_WORD) ?: ""
        if (mKeyword != "") {
            mViewModel.gifDataList.clear()
            mAdapter?.notifyDataSetChanged()
            mBinding.etSearch?.setText(mKeyword)
            mBinding.etSearch?.setSelection(mKeyword.length)
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
        var offset = SpHelper.getString(Global.SP_SEARCH_OFFSET, "0")
        log("offset = $offset")
        showLoadingProgressDialog(getString(R.string.searching))

        mViewModel.viewModelScope.launch(Dispatchers.Main) {
            val gifItemList = Repository.search(keyword, offset)
            hideLoadingProgressDialog()
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

            SpHelper.putString(Global.SP_SEARCH_OFFSET, offset)

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
//        AdChainHelper.loadAd(
//            AdConstants.AD_NAME_DETAIL_INSERT,
//            null,
//            object : SimpleAdChainListener {
//                override fun onLoaded(ad: IAd?) {
//                    mDetailInsertAd = ad
//                }
//            })
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