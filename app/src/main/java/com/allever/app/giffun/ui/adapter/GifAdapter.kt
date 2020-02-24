package com.allever.app.giffun.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.allever.app.giffun.BuildConfig

import com.allever.app.giffun.R
import com.allever.app.giffun.app.Global
import com.allever.app.giffun.bean.DataBean
import com.allever.app.giffun.bean.event.LikeEvent
import com.allever.app.giffun.function.download.DownloadCallback
import com.allever.app.giffun.function.download.DownloadManager
import com.allever.app.giffun.function.download.TaskInfo
import com.allever.app.giffun.ui.SearchActivity
import com.allever.app.giffun.util.DBHelper
import com.allever.app.giffun.util.MD5
import com.allever.lib.common.app.App
import com.allever.lib.common.ui.widget.recycler.BaseRecyclerViewAdapter
import com.allever.lib.common.ui.widget.recycler.BaseViewHolder
import com.allever.lib.common.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import org.greenrobot.eventbus.EventBus

import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView

import java.io.File
import java.lang.Exception

class GifAdapter(context: Context, resId: Int, data: MutableList<DataBean>) :
    BaseRecyclerViewAdapter<DataBean>(context, resId, data) {

    @SuppressLint("SetTextI18n")
    override fun bindHolder(holder: BaseViewHolder, position: Int, item: DataBean) {
        //debug
        val tvLoadUrl = holder.getView<TextView>(R.id.tvUrl)
        val tvSize = holder.getView<TextView>(R.id.tvSize)
        val tvTempPath = holder.getView<TextView>(R.id.tvFilePath)
        val tvStatus = holder.getView<TextView>(R.id.tvStatus)

        val gifUrl = item.images.fixed_height.url
        val fileName = MD5.getMD5Str(item.id) + ".gif"
        val tempPath = "${Global.tempDir}${File.separator}$fileName"
        val cachePath = "${Global.cacheDir}${File.separator}$fileName"
        val savePath = "${Global.saveDir}${File.separator}$fileName"
        var drawable: GifDrawable? = null
        var downloaded = FileUtils.checkExist(tempPath)

        val gifImageView = holder.getView<GifImageView>(R.id.gifImageView)
        val ivPlay = holder.getView<ImageView>(R.id.ivPlay)
        val ivRetry = holder.getView<ImageView>(R.id.ivRetry)
        val progressLoading = holder.getView<ProgressBar>(R.id.progressCircle)
        val downloadProgressBar = holder.getView<ProgressBar>(R.id.downloadProgress)

        val tvTitle = holder.getView<TextView>(R.id.tvTitle)
        tvTitle?.text = item.title
        val tvDisplayName = holder.getView<TextView>(R.id.tvDisplayName)
        val displayName = item.user?.display_name ?: ""
        tvDisplayName?.text = "@$displayName"


        val ivHeader = holder.getView<ImageView>(R.id.ivHeader)
        Glide.with(App.context).load(item.user?.avatar_url).into(ivHeader!!)

        val ivLike = holder.getView<ImageView>(R.id.ivLike)
        val ivShare = holder.getView<ImageView>(R.id.ivShare)
        val ivDownload = holder.getView<ImageView>(R.id.ivDownload)
        val ivMore = holder.getView<ImageView>(R.id.ivMore)
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
                tvStatus?.text = "状态：开始下载"
                progressLoading?.visibility = VISIBLE
                ivPlay?.visibility = GONE
                ivRetry?.visibility = GONE
                gifImageView?.visibility = GONE
            }

            override fun onConnected(totalLength: Long) {
                tvStatus?.text = "状态：已连接"
                downloadProgressBar?.max = 100
                gifImageView?.visibility = GONE
            }

            override fun onProgress(current: Long, totalLength: Long) {
                val percent = ((current / totalLength.toFloat()) * 100).toInt()
                downloadProgressBar?.progress = percent

                log("进度: $percent")
                tvStatus?.text = "状态：下载中: $current"
            }

            override fun onPause(taskInfo: TaskInfo?) {
                tvStatus?.text = "状态：暂停下载"

                progressLoading?.visibility = GONE
                ivPlay?.visibility = GONE
                ivRetry?.visibility = VISIBLE

            }

            override fun onCompleted(taskInfo: TaskInfo?) {
                downloadProgressBar?.progress = 100
                gifImageView?.visibility = VISIBLE
                tvStatus?.text = "状态：下载完成"
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
                progressLoading?.visibility = GONE
                ivPlay?.visibility = GONE
                ivRetry?.visibility = GONE
            }

            override fun onError(e: Exception?) {
                tvStatus?.text = "状态：下载出错"
                progressLoading?.visibility = GONE
                ivPlay?.visibility = GONE
                ivRetry?.visibility = VISIBLE
                gifImageView?.visibility = GONE
            }

        }

        gifImageView?.setOnClickListener {

        }

        ivPlay?.setOnClickListener {
            ivPlay.visibility = GONE
            if (FileUtils.checkExist(tempPath)) {
                progressLoading?.visibility = GONE
                ivRetry?.visibility = GONE
                drawable?.start()
            } else {
                val task = TaskInfo(fileName, Global.cacheDir, gifUrl)
                DownloadManager.getInstance().start(task, downloadCallback, true)
            }
        }

        ivRetry?.setOnClickListener {
            val task = TaskInfo(fileName, Global.cacheDir, gifUrl)
            DownloadManager.getInstance().start(task, downloadCallback)
        }


        val liked = DBHelper.isLiked(item.id)
        if (liked) {
            ivLike?.setColorFilter(mContext.resources.getColor(R.color.default_theme_color))
        } else {
            ivLike?.colorFilter = null
        }

        ivLike?.setOnClickListener {
            val liked = DBHelper.isLiked(item.id)
            val likeEvent = LikeEvent()
            likeEvent.id = item.id
            likeEvent.dataBean = item
            if (liked) {
                ivLike.colorFilter = null
                DBHelper.unLiked(item.id)
                likeEvent.isLiked = false
            } else {
                ivLike.setColorFilter(mContext.resources.getColor(R.color.default_theme_color))
                DBHelper.liked(item.id, item)
                likeEvent.isLiked = true
            }

            EventBus.getDefault().post(likeEvent)
        }

        ivShare?.setOnClickListener {
            if (FileUtils.checkExist(tempPath)) {
                ShareHelper.shareImage(mContext, tempPath)
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
            val title = item.title
            var searchContent: String = ""
            if (title.contains("GIF")) {
                val titleArray = title.split("GIF")
                if (titleArray.isNotEmpty()) {
                    searchContent = titleArray[0]
                }
            }

            log("搜索关键字： $searchContent")
            SearchActivity.start(App.context, searchContent)
        }


        log("load url = $gifUrl")
        tvLoadUrl?.text = gifUrl
        tvSize?.text = item.images.fixed_height.size

        if (FileUtils.checkExist(tempPath)) {
            downloadProgressBar?.progress = 100
            tvStatus?.text = "状态：已下载"
//            drawable = GifDrawable(tempPath)
//            gifImageView?.setImageDrawable(drawable)
            Glide.with(App.context)
                .load(tempPath)
                .skipMemoryCache(true)
                .diskCacheStrategy(
                    DiskCacheStrategy.NONE
                )
                .into(gifImageView!!)

            progressLoading?.visibility = GONE
            ivPlay?.visibility = GONE
            ivRetry?.visibility = GONE
            gifImageView.visibility = VISIBLE

            return
        }


//        if (BuildConfig.DEBUG) {
//            return
//        }

        val task = TaskInfo(fileName, Global.cacheDir, gifUrl)
        DownloadManager.getInstance().start(task, downloadCallback, true)

    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        super.onViewRecycled(holder)
        val position = holder.adapterPosition
        log("回收view： $position")
        val imageView = holder.getView<GifImageView>(R.id.gifImageView)
        if (imageView != null) {
            Glide.with(mContext).clear(imageView)
        }
//        val gifUrl = mData[position].images.fixed_height.url
//        DownloadManager.getInstance().pause(gifUrl)
    }

    companion object {
        private const val STATUS_PLAY = 0
        private const val STATUS_PAUSE = 1
        private const val STATUS_RETRY = 2
        private const val STATUS_HIDE = 4
    }
}