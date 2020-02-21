package com.allever.app.giffun.ui

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.allever.app.giffun.BuildConfig
import com.allever.app.giffun.R
import com.allever.app.giffun.app.Global
import com.allever.app.giffun.bean.DataBean
import com.allever.app.giffun.bean.event.LikeEvent
import com.allever.app.giffun.bean.event.DownloadFinishEvent
import com.allever.app.giffun.function.download.DownloadCallback
import com.allever.app.giffun.function.download.DownloadManager
import com.allever.app.giffun.function.download.TaskInfo
import com.allever.app.giffun.util.DBHelper
import com.allever.app.giffun.util.MD5
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
import kotlin.Exception

class GifPreviewActivity : BaseActivity() {

    private var mIsDownloadFinish = false

    private lateinit var item: DataBean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gif_preview)

        init()

    }

    private fun init() {
        intent ?: return

        val dataJson = intent.getStringExtra(EXTRA_DATA_BEAN_JSON) ?: return

        try {
            item = Gson().fromJson(dataJson, DataBean::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        item?:return


        val gifId = item.id
        val gifUrl = item.images.fixed_height.url
        val headerUrl = item.user?.avatar_url ?: ""
        val userName = item.user?.display_name ?: ""
        val title = item.title ?: ""


//        val gifUrl = item.images.fixed_height.url
        val fileName = MD5.getMD5Str(gifId) + ".gif"
        val tempPath = "${Global.tempDir}${File.separator}$fileName"
        val cachePath = "${Global.cacheDir}${File.separator}$fileName"
        val savePath = "${Global.saveDir}${File.separator}$fileName"
        var drawable: GifDrawable? = null
        var downloaded = FileUtils.checkExist(tempPath)

        val gifImageView = findViewById<GifImageView>(R.id.gifImageView)
        val ivPlay = findViewById<ImageView>(R.id.ivPlay)
        val ivRetry = findViewById<ImageView>(R.id.ivRetry)
        val progressLoading = findViewById<ProgressBar>(R.id.progressCircle)

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
                gifImageView?.visibility = View.GONE
            }

            override fun onProgress(current: Long, totalLength: Long) {
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
                DownloadManager.getInstance().start(task, downloadCallback, true)
            }
        }

        ivRetry?.setOnClickListener {
            val task = TaskInfo(fileName, Global.cacheDir, gifUrl)
            DownloadManager.getInstance().start(task, downloadCallback, true)
        }


        val liked = DBHelper.isLiked(gifId)
        if (liked) {
            ivLike?.setColorFilter(resources.getColor(R.color.default_theme_color))
        } else {
            ivLike?.colorFilter = null
        }

        ivLike?.setOnClickListener {
            val liked = DBHelper.isLiked(gifId)
            val likeEvent = LikeEvent()
            likeEvent.id = gifId
            likeEvent.dataBean = item
            if (liked) {
                ivLike.colorFilter = null
                DBHelper.unLiked(gifId)
                likeEvent.isLiked = false
            } else {
                ivLike.setColorFilter(resources.getColor(R.color.default_theme_color))
                DBHelper.liked(gifId, item)
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


        if (BuildConfig.DEBUG) {
            return
        }

        val task = TaskInfo(fileName, Global.cacheDir, gifUrl)
        DownloadManager.getInstance().start(task, downloadCallback, true)

    }


    override fun onDestroy() {
        super.onDestroy()
        if (item != null) {
            DownloadManager.getInstance().cancel(item.images.fixed_height.url)
        }

        if (mIsDownloadFinish) {
            EventBus.getDefault().post(DownloadFinishEvent())
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