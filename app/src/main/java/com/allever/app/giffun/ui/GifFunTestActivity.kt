package com.allever.app.giffun.ui

import android.Manifest
import android.os.Bundle
import android.os.Environment
import androidx.recyclerview.widget.LinearLayoutManager
import com.allever.app.giffun.R
import com.allever.app.giffun.bean.*
import com.allever.app.giffun.function.download.DownloadManager
import com.allever.app.giffun.ui.adapter.GifTestAdapter
import com.allever.app.giffun.ui.mvp.model.RetrofitUtil
import com.allever.app.giffun.util.SpUtils
import com.allever.lib.common.app.App
import com.allever.lib.common.app.BaseActivity
import com.allever.lib.common.util.FileUtil
import com.allever.lib.common.util.FileUtils
import com.allever.lib.common.util.ShareHelper
import com.allever.lib.common.util.log
import com.allever.lib.permission.PermissionListener
import com.allever.lib.permission.PermissionManager
import com.google.android.gms.common.util.SharedPreferencesUtils
import kotlinx.android.synthetic.main.activity_giffun_test.*
import rx.Subscriber
import java.io.File


class GifFunTestActivity: BaseActivity() {


    private var mAdapter: GifTestAdapter? = null
    private var mGifDataList = mutableListOf<DataBean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_giffun_test)

        val dir = Environment.getExternalStorageDirectory().absoluteFile.path + File.separator + App.context.packageName + File.separator + "gifFunny"
        if (!FileUtils.checkExist(dir)) {
            FileUtil.createDir(dir)
        }

        val tempDir = App.context.cacheDir.absolutePath + File.separator +"gif"
        if (!FileUtils.checkExist(tempDir)) {
            FileUtil.createDir(tempDir)
        }

        PermissionManager.request(object : PermissionListener {
            override fun onGranted(grantedList: MutableList<String>) {

            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        mAdapter = GifTestAdapter(this, R.layout.item_test_radom, mGifDataList)

        rvGif.layoutManager = LinearLayoutManager(this)
        rvGif.adapter = mAdapter
        
        etType.setText(SpUtils.getString(SP_TYPE, "search"))
        etKeyword.setText(SpUtils.getString(SP_KEYWORD, "cat"))
        etOffset.setText(SpUtils.getString(SP_OFFSET, "0"))
        etCount.setText(SpUtils.getString(SP_COUNT, "10"))

        btnFetch.setOnClickListener {
            val type = etType.text.toString()
            val keyword = etKeyword.text.toString()
            val offset = etOffset.text.toString()
            val count = etCount.text.toString()
            
            if (type != "") {
                SpUtils.putString(SP_TYPE, type)
            }
            
            if (keyword != "") {
                SpUtils.putString(SP_KEYWORD, keyword)
            }
            
            if (offset != "") {
                SpUtils.putString(SP_OFFSET, offset)
            }
            
            if (count != "") {
                SpUtils.putString(SP_COUNT, count)
            }

            tvTips.text = "正在加载数据"

            when (type) {
                "search" -> {
                    RetrofitUtil.searchGif(keyword, offset.toInt(), count.toInt(), object : Subscriber<SearchResponse>() {
                        override fun onCompleted() {}
                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                            log("失败")
                            tvTips.text = "请求失败"
                        }

                        override fun onNext(bean: SearchResponse) {
                            tvTips.text = "请求成功"
                            val data = bean.data
                            data?.map {
                                log("search = ${it.images.preview_gif.url}")
                            }

                            mGifDataList.clear()
                            mGifDataList.addAll(data)
                            mAdapter?.notifyDataSetChanged()
                        }
                    })
                }
                "trans" -> {
                    RetrofitUtil.translateGif(keyword,  object : Subscriber<TranslateResponse>() {
                        override fun onCompleted() {}
                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                            log("失败")
                            tvTips.text = "请求失败"
                        }

                        override fun onNext(bean: TranslateResponse) {
                            tvTips.text = "请求成功"
                            val data = bean.data
                            mGifDataList.clear()
                            mGifDataList.add(data)
                            mAdapter?.notifyDataSetChanged()
                        }
                    })
                }
                "trend" -> {
                    RetrofitUtil.trendingGif(offset.toInt(), count.toInt(), object : Subscriber<TrendingResponse>() {
                        override fun onCompleted() {}
                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                            log("失败")
                            tvTips.text = "请求失败"
                        }

                        override fun onNext(bean: TrendingResponse) {
                            tvTips.text = "请求成功"
                            val data = bean.data
                            data?.map {
                                log("trending = ${it.images.original.url}")
                            }

                            mGifDataList.clear()
                            mGifDataList.addAll(data)
                            mAdapter?.notifyDataSetChanged()
                        }
                    })
                }
                "random" -> {
                    RetrofitUtil.getRandomGif(object : Subscriber<RandomResponse>() {
                        override fun onCompleted() {}
                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                            log("失败")
                            tvTips.text = "请求失败"
                        }

                        override fun onNext(bean: RandomResponse) {
                            tvTips.text = "请求成功"
                            val data = bean.data
                            log("random = ${data.images.preview_gif.url}")

                            tvResult.text = "成功： url = ${data.images.preview_gif.url}"

                            mGifDataList.clear()
                            mGifDataList.add(data)
                            mAdapter?.notifyDataSetChanged()
                        }
                    })
                }
                else -> {
                    tvTips.text = "类型错误"
                }
            }
        }


//        featchData()










//        GiphyCoreUI.configure(this, "ENMowe3QQJBeL3fHproYw7C67ignSnuL")
//
//        val mediaView = GPHMediaView(this@GifFunTestActivity)
//
//
//        GiphyDialogFragment.newInstance().show(supportFragmentManager, "giphy_dialog")
//
//        val client = GPHApiClient("ENMowe3QQJBeL3fHproYw7C67ignSnuL")
//        client.search(
//            "cats",
//            MediaType.gif,
//            null,
//            null,
//            null,
//            null,null,
//            object : CompletionHandler<ListMediaResponse> {
//                override fun onComplete(result: ListMediaResponse?, e: Throwable?) {
//                    if (result == null) {
//                        // Do what you want to do with the error
//                    } else {
//                        if (result.data != null) {
//                            for (gif in result.data!!) {
//                                Log.v("giphy", gif.id)
//                            }
//
//                            if (result.data?.isNotEmpty() == true) {
//                                val media = result.data?.get(0)
//                                gifView.setMedia(media, RenditionType.original)
//                            }
//                        } else {
//                            Log.e("giphy error", "No results found")
//                        }
//                    }
//                }
//            })
//


//        val settings = GPHSettings(gridType = GridType.waterfall, theme = LightTheme, dimBackground = true)
//        settings.gridType = GridType.waterfall
//        settings.mediaTypeConfig = arrayOf(GPHContentType.gif, GPHContentType.sticker, GPHContentType.text, GPHContentType.emoji)
//        settings.showConfirmationScreen = true
//        settings.rating = RatingType.pg13
//        settings.renditionType = RenditionType.fixedWidth
//        settings.confirmationRenditionType = RenditionType.original
//        val gifsDialog = GiphyDialogFragment.newInstance(settings)
//
//        gifsDialog.gifSelectionListener = object : GiphyDialogFragment.GifSelectionListener {
//            override fun onDismissed() {
////                gifsDialog.show(supportFragmentManager, "gifs_dialog")
//            }
//
//            override fun onGifSelected(media: Media) {
//                mediaView.setMedia(media, RenditionType.original)
//            }
//
//        }
//
//        gifsDialog.show(supportFragmentManager, "gifs_dialog")
    }


    private fun featchData() {

    }

    override fun onDestroy() {
        super.onDestroy()
        DownloadManager.getInstance().cancelAllTask()
    }
    
    companion object {
        private const val SP_TYPE = "type"
        private const val SP_KEYWORD = "keyword"
        private const val SP_OFFSET = "offset"
        private const val SP_COUNT = "count"
    }
}