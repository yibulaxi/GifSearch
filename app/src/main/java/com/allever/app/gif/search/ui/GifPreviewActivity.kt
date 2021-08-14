package com.allever.app.gif.search.ui

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.allever.app.gif.search.R
import com.allever.app.gif.search.app.Global
import com.allever.app.gif.search.bean.event.DownloadFinishEvent
import com.allever.app.gif.search.bean.event.LikeEvent
import com.allever.app.gif.search.function.download.DownloadCallback
import com.allever.app.gif.search.function.download.DownloadManager
import com.allever.app.gif.search.function.download.TaskInfo
import com.allever.app.gif.search.ui.adapter.bean.GifItem
import com.allever.app.gif.search.ui.search.SearchActivity
import com.allever.app.gif.search.util.DBHelper
import com.allever.app.gif.search.util.MD5
import com.allever.lib.common.app.App
import com.allever.lib.common.app.BaseActivity
import com.allever.lib.common.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView
import java.io.File

class GifPreviewActivity : BaseActivity() {

    private var mIsDownloadFinish = false

    private lateinit var item: GifItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gif_preview)

        init()

    }

    private fun init() {
        intent ?: return

        val dataJson = intent.getStringExtra(EXTRA_DATA_BEAN_JSON) ?: return

        try {
            item = Gson().fromJson(dataJson, GifItem::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        item ?: return


        val gifId = item.id
        val gifUrl = item.url
        val headerUrl = item.avatar
        val userName = item.nickname
        val title = item.title ?: ""


//        val gifUrl = item.images.fixed_height.url
        val fileName = MD5.getMD5Str(gifId.toString()) + ".gif"
        val tempPath = "${Global.tempDir}${File.separator}$fileName"
        val cachePath = "${Global.cacheDir}${File.separator}$fileName"
        val savePath = "${Global.saveDir}${File.separator}$fileName"
        var drawable: GifDrawable? = null
        var downloaded = FileUtils.checkExist(tempPath)

        val gifImageView = findViewById<GifImageView>(R.id.gifImageView)
        val ivPlay = findViewById<ImageView>(R.id.ivPlay)
        val ivRetry = findViewById<ImageView>(R.id.ivRetry)
        val progressLoading = findViewById<ProgressBar>(R.id.progressCircle)
        val downloadProgressBar = findViewById<ProgressBar>(R.id.downloadProgress)
        downloadProgressBar?.progress = 0

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        tvTitle?.text = title
        val tvDisplayName = findViewById<TextView>(R.id.tvDisplayName)
        val displayName = userName ?: ""
        tvDisplayName?.text = "@$displayName"


        val ivHeader = findViewById<ImageView>(R.id.ivHeader)
        Glide.with(App.context).load(headerUrl).into(ivHeader!!)

        val ivLike = findViewById<ImageView>(R.id.ivLike)
        val ivShare = findViewById<ImageView>(R.id.ivShare)
        val ivDownload = findViewById<ImageView>(R.id.ivDownload)
        val ivMore = findViewById<ImageView>(R.id.ivMore)
        if (FileUtils.checkExist(savePath)) {
            ivDownload?.setColorFilter(
                App.context.resources.getColor(R.color.gray_66),
                PorterDuff.Mode.SRC_IN
            )
        } else {
            ivDownload?.colorFilter = null
        }

        val downloadCallback = object : DownloadCallback {
            override fun onStart() {
                progressLoading?.visibility = View.VISIBLE
                ivPlay?.visibility = View.GONE
                ivRetry?.visibility = View.GONE
                gifImageView?.visibility = View.GONE
            }

            override fun onConnected(totalLength: Long) {
                downloadProgressBar?.max = 100
                gifImageView?.visibility = View.GONE
            }

            override fun onProgress(current: Long, totalLength: Long) {
                val percent = ((current / totalLength.toFloat()) * 100).toInt()
                downloadProgressBar?.progress = percent
            }

            override fun onPause(taskInfo: TaskInfo?) {
                progressLoading?.visibility = View.GONE
                ivPlay?.visibility = View.GONE
                ivRetry?.visibility = View.VISIBLE

            }

            override fun onCompleted(taskInfo: TaskInfo?) {
                mIsDownloadFinish = true
                gifImageView?.visibility = View.VISIBLE
                FileUtil.createNewFile(tempPath, false)
                com.android.absbase.utils.FileUtils.copyFile(cachePath, tempPath, true)
//                val drawable = GifDrawable(tempPath)
//                gifImageView?.setImageDrawable(drawable)
                Glide.with(App.context)
                    .load(tempPath)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(
                        DiskCacheStrategy.NONE
                    )
                    .into(gifImageView!!)
                progressLoading?.visibility = View.GONE
                ivPlay?.visibility = View.GONE
                ivRetry?.visibility = View.GONE
            }

            override fun onError(e: Exception?) {
                progressLoading?.visibility = View.GONE
                ivPlay?.visibility = View.GONE
                ivRetry?.visibility = View.VISIBLE
                gifImageView?.visibility = View.GONE
            }

        }

        gifImageView?.setOnClickListener {

        }

        ivPlay?.setOnClickListener {
            ivPlay.visibility = View.GONE
            if (FileUtils.checkExist(tempPath)) {
                progressLoading?.visibility = View.GONE
                ivRetry?.visibility = View.GONE
                drawable?.start()
            } else {
                val task = TaskInfo(fileName, Global.cacheDir, gifUrl)
                DownloadManager.getInstance().start(task, downloadCallback)
            }
        }

        ivRetry?.setOnClickListener {
            val task = TaskInfo(fileName, Global.cacheDir, gifUrl)
            DownloadManager.getInstance().start(task, downloadCallback)
        }


        val liked = DBHelper.isLiked(gifId.toString())
        if (liked) {
            ivLike?.setColorFilter(resources.getColor(R.color.default_theme_color))
        } else {
            ivLike?.colorFilter = null
        }

        ivLike?.setOnClickListener {
            val liked = DBHelper.isLiked(gifId.toString())
            val likeEvent = LikeEvent()
            likeEvent.id = gifId.toString()
            likeEvent.dataBean = item
            if (liked) {
                ivLike.colorFilter = null
                DBHelper.unLiked(gifId.toString())
                likeEvent.isLiked = false
            } else {
                ivLike.setColorFilter(resources.getColor(R.color.default_theme_color))
                DBHelper.liked(gifId.toString(), item)
                likeEvent.isLiked = true
            }

            EventBus.getDefault().post(likeEvent)
        }

        ivShare?.setOnClickListener {
            if (FileUtils.checkExist(tempPath)) {
                ShareHelper.shareImage(this, tempPath)
            } else {
                toast(R.string.file_not_found)
            }
        }

        ivDownload?.setOnClickListener {
            if (FileUtils.checkExist(savePath)) {
                toast(R.string.already_download)
                return@setOnClickListener
            }
            if (FileUtils.checkExist(tempPath)) {
                FileUtil.createNewFile(savePath, false)
                com.android.absbase.utils.FileUtils.copyFile(tempPath, savePath, true)
                toast("${getString(R.string.already_save_to)}\n$savePath")
                ivDownload.setColorFilter(
                    App.context.resources.getColor(R.color.gray_66),
                    PorterDuff.Mode.SRC_IN
                )
            } else {
                toast(R.string.file_not_found)
            }

        }

        ivMore?.setOnClickListener {
            var searchContent: String = ""
            if (title?.contains("GIF") == true) {
                val titleArray = title.split("GIF")
                if (titleArray.isNotEmpty()) {
                    searchContent = titleArray[0]
                }
            }

            log("搜索关键字： $searchContent")
            SearchActivity.start(App.context, searchContent)
        }

        log("load url = $gifUrl")

        if (FileUtils.checkExist(tempPath)) {
            downloadProgressBar?.progress = 100
//            drawable = GifDrawable(tempPath)
//            gifImageView?.setImageDrawable(drawable)
            Glide.with(App.context)
                .load(tempPath)
                .skipMemoryCache(true)
                .diskCacheStrategy(
                    DiskCacheStrategy.NONE
                )
                .into(gifImageView!!)

            progressLoading?.visibility = View.GONE
            ivPlay?.visibility = View.GONE
            ivRetry?.visibility = View.GONE
            gifImageView.visibility = View.VISIBLE

            return
        }


//        if (BuildConfig.DEBUG) {
//            return
//        }

        DownloadManager.getInstance().cancel(gifUrl)
        val task = TaskInfo(fileName, Global.cacheDir, gifUrl)
        DownloadManager.getInstance().start(task, downloadCallback)

    }


    override fun onDestroy() {
        super.onDestroy()
        if (item != null) {
            DownloadManager.getInstance().cancel(item.url)
        }

        if (mIsDownloadFinish) {
            val event = DownloadFinishEvent()
            event.id = item.id.toString()
            EventBus.getDefault().post(event)
        }
    }

    companion object {
        private const val EXTRA_DATA_BEAN_JSON = "EXTRA_DATA_BEAN_JSON"
        fun start(
            context: Context,
            dataJson: String
        ) {
            val intent = Intent(context, GifPreviewActivity::class.java)
            intent.putExtra(EXTRA_DATA_BEAN_JSON, dataJson)
            context.startActivity(intent)
        }
    }
}