package com.funny.app.gif.memes.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.funny.app.gif.memes.R
import com.funny.app.gif.memes.app.GlobalObj
import com.funny.app.gif.memes.bean.event.LikeGifEvent
import com.funny.app.gif.memes.function.download.DownloadCallback
import com.funny.app.gif.memes.function.download.DownloadManager
import com.funny.app.gif.memes.function.download.TaskInfo
import com.funny.app.gif.memes.function.network.NetRepository
import com.funny.app.gif.memes.function.store.Store
import com.funny.app.gif.memes.ui.search.SearchGifActivity
import com.funny.app.gif.memes.ui.adapter.bean.GifItemBean
import com.funny.app.gif.memes.util.DataBaseHelper
import com.funny.app.gif.memes.util.MD5
import com.funny.lib.common.app.App
import com.funny.lib.common.ui.widget.recycler.BaseRecyclerViewAdapter
import com.funny.lib.common.ui.widget.recycler.BaseViewHolder
import com.funny.lib.common.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView
import java.io.File

class GifItemAdapter(context: Context, resId: Int, data: MutableList<GifItemBean>) :
    BaseRecyclerViewAdapter<GifItemBean>(context, resId, data) {

    @SuppressLint("SetTextI18n")
    override fun bindHolder(holder: BaseViewHolder, position: Int, item: GifItemBean) {
        //debug
        val tvLoadUrl = holder.getView<TextView>(R.id.tvUrl)
        val tvSize = holder.getView<TextView>(R.id.tvSize)
        val tvTempPath = holder.getView<TextView>(R.id.tvFilePath)
        val tvStatus = holder.getView<TextView>(R.id.tvStatus)

        val gifUrl = item.url
        val fileName = MD5.getMD5Str(item.id.toString()) + ".gif"
        val tempPath = "${GlobalObj.tempDir}${File.separator}$fileName"
        val cachePath = "${GlobalObj.cacheDir}${File.separator}$fileName"
        val savePath = "${GlobalObj.saveDir}${File.separator}$fileName"
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
        val displayName = item.nickname
        tvDisplayName?.text = "@$displayName"


        val ivHeader = holder.getView<ImageView>(R.id.ivHeader)
        Glide.with(App.context).load(item.avatar).into(ivHeader!!)

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
                val task = TaskInfo(fileName, GlobalObj.cacheDir, gifUrl)
                DownloadManager.getInstance().start(task, downloadCallback, true)
            }
        }

        ivRetry?.setOnClickListener {
            val task = TaskInfo(fileName, GlobalObj.cacheDir, gifUrl)
            DownloadManager.getInstance().start(task, downloadCallback)
        }


        val liked = DataBaseHelper.isLiked(item.id.toString())
        if (liked) {
            ivLike?.setColorFilter(mContext.resources.getColor(R.color.default_theme_color))
        } else {
            ivLike?.colorFilter = null
        }

        ivLike?.setOnClickListener {
            val liked = DataBaseHelper.isLiked(item.id.toString())
            val likeEvent = LikeGifEvent()
            likeEvent.id = item.id.toString()
            likeEvent.type = item.type
            likeEvent.dataBean = item
            if (liked) {
                ivLike.colorFilter = null
                DataBaseHelper.unLiked(item.id.toString())
                likeEvent.isLiked = false
            } else {
                ivLike.setColorFilter(mContext.resources.getColor(R.color.default_theme_color))
                DataBaseHelper.liked(item.id.toString(), item)
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
            SearchGifActivity.start(App.context, searchContent)
        }


        log("load url = $gifUrl")
        tvLoadUrl?.text = gifUrl
        tvSize?.text = item.size.toString()

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

        val task = TaskInfo(fileName, GlobalObj.cacheDir, gifUrl)
        if (gifUrl.isNotEmpty()) {
            DownloadManager.getInstance().start(task, downloadCallback, true)
        }

        if (item.url.isEmpty()) {
            GlobalScope.launch(Dispatchers.Main) {
                val response = NetRepository.getAuthorizedUrl(Store.getToken(), Store.getUserId().toString(), item.id, item.tempUrl)
                response.data?.let {
                    log("真实url = ${it.authorizeUrl}")
                    item.url = it.authorizeUrl
                    DownloadManager.getInstance().start(task, downloadCallback, true)
                    notifyItemChanged(position, position)
                }
            }
        }
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