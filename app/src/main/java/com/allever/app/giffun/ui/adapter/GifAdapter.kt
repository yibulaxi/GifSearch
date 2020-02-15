package com.allever.app.giffun.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.text.TextUtils
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView

import com.allever.app.giffun.R
import com.allever.app.giffun.app.Global
import com.allever.app.giffun.bean.DataBean
import com.allever.app.giffun.function.download.DownloadCallback
import com.allever.app.giffun.function.download.DownloadManager
import com.allever.app.giffun.function.download.TaskInfo
import com.allever.app.giffun.util.MD5
import com.allever.lib.common.ui.widget.custom.CircleImageView
import com.allever.lib.common.ui.widget.recycler.BaseRecyclerViewAdapter
import com.allever.lib.common.ui.widget.recycler.BaseViewHolder
import com.allever.lib.common.util.*
import com.bumptech.glide.Glide

import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView

import java.io.File
import java.lang.Exception

class GifAdapter(context: Context, resId: Int, data: MutableList<DataBean>) :
    BaseRecyclerViewAdapter<DataBean>(context, resId, data) {

    @SuppressLint("SetTextI18n")
    override fun bindHolder(holder: BaseViewHolder, position: Int, item: DataBean) {
        //debug
        val progressBarDebug = holder.getView<ProgressBar>(R.id.downloadProgress)
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

        val tvTitle = holder.getView<TextView>(R.id.tvTitle)
        tvTitle?.text = item.title
        val tvDisplayName = holder.getView<TextView>(R.id.tvDisplayName)
        val displayName = item.user?.display_name ?:""
        tvDisplayName?.text = "@$displayName"


        val ivHeader = holder.getView<ImageView>(R.id.ivHeader)
        Glide.with(mContext).load(item.user?.avatar_url).into(ivHeader!!)

        val ivLike = holder.getView<ImageView>(R.id.ivLike)
        val ivShare = holder.getView<ImageView>(R.id.ivShare)
        val ivDownload = holder.getView<ImageView>(R.id.ivDownload)
        val ivMore = holder.getView<ImageView>(R.id.ivMore)
        if (FileUtils.checkExist(savePath)) {
            ivDownload?.setColorFilter(mContext.resources.getColor(R.color.gray_66),PorterDuff.Mode.SRC_IN)
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
                progressBarDebug?.max = totalLength.toInt()
                gifImageView?.visibility = GONE
            }

            override fun onProgress(current: Long, totalLength: Long) {
                progressBarDebug?.progress = current.toInt()
                tvStatus?.text = "状态：下载中: $current"
            }

            override fun onPause(taskInfo: TaskInfo?) {
                tvStatus?.text = "状态：暂停下载"

                progressLoading?.visibility = GONE
                ivPlay?.visibility = GONE
                ivRetry?.visibility = VISIBLE

            }

            override fun onCompleted(taskInfo: TaskInfo?) {
                gifImageView?.visibility = VISIBLE
                tvStatus?.text = "状态：下载完成"
                FileUtil.createNewFile(tempPath, false)
                com.android.absbase.utils.FileUtils.copyFile(cachePath, tempPath, true)
//                val drawable = GifDrawable(tempPath)
//                gifImageView?.setImageDrawable(drawable)
                Glide.with(mContext).load(tempPath).into(gifImageView!!)
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
            ToastUtils.show("重新下载")
            val task = TaskInfo(fileName, Global.cacheDir, gifUrl)
            DownloadManager.getInstance().start(task, downloadCallback, true)
        }

        ivLike?.setOnClickListener {
            ToastUtils.show("like")
        }

        ivShare?.setOnClickListener {
            if (FileUtils.checkExist(tempPath)) {
                ShareHelper.shareImage(mContext, tempPath)
            } else {
                ToastUtils.show("文件不存在")
            }
        }

        ivDownload?.setOnClickListener {
//            ToastUtils.show("保存")
            if (FileUtils.checkExist(savePath)) {
                ToastUtils.show("已下载")
                return@setOnClickListener
            }
            if (FileUtils.checkExist(tempPath)) {
                FileUtil.createNewFile(savePath, false)
                com.android.absbase.utils.FileUtils.copyFile(tempPath, savePath, true)
                ToastUtils.show("已保存到：\n$savePath")
                ivDownload.setColorFilter(mContext.resources.getColor(R.color.gray_66),PorterDuff.Mode.SRC_IN)
            } else {
                toast("文件不存在")
            }

        }

        ivMore?.setOnClickListener {
            ToastUtils.show("更多")
        }


        progressBarDebug?.progress = 0
        log("load url = $gifUrl")
        tvLoadUrl?.text = gifUrl
        tvSize?.text = item.images.fixed_height.size

        if (FileUtils.checkExist(tempPath)) {
            tvStatus?.text = "状态：已下载"
//            drawable = GifDrawable(tempPath)
//            gifImageView?.setImageDrawable(drawable)
            Glide.with(mContext).load(tempPath).into(gifImageView!!)

            progressLoading?.visibility = GONE
            ivPlay?.visibility = GONE
            ivRetry?.visibility = GONE
            gifImageView.visibility = VISIBLE

            return
        }



        val task = TaskInfo(fileName, Global.cacheDir, gifUrl)
        DownloadManager.getInstance().start(task, downloadCallback, true)

    }

    companion object {
        private const val STATUS_PLAY = 0
        private const val STATUS_PAUSE = 1
        private const val STATUS_RETRY = 2
        private const val STATUS_HIDE = 4
    }
}