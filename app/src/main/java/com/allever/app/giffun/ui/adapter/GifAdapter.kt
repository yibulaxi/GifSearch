package com.allever.app.giffun.ui.adapter

import android.content.Context
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
import com.allever.lib.common.util.FileUtil
import com.allever.lib.common.util.FileUtils
import com.allever.lib.common.util.ToastUtils
import com.allever.lib.common.util.log
import com.bumptech.glide.Glide

import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView

import java.io.File
import java.lang.Exception

class GifAdapter(context: Context, resId: Int, data: MutableList<DataBean>) :
    BaseRecyclerViewAdapter<DataBean>(context, resId, data) {

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
        var drawable: GifDrawable? = null
        var downloaded = FileUtils.checkExist(tempPath)

        val gifImageView = holder.getView<GifImageView>(R.id.gifImageView)
        val ivPlay = holder.getView<ImageView>(R.id.ivPlay)
        val ivRetry = holder.getView<ImageView>(R.id.ivRetry)
        val progressLoading = holder.getView<ProgressBar>(R.id.progressCircle)

        val tvTitle = holder.getView<TextView>(R.id.tvTitle)
        tvTitle?.text = item.title
        val tvDisplayName = holder.getView<TextView>(R.id.tvDisplayName)
        tvDisplayName?.text = item.user?.display_name

        val ivHeader = holder.getView<ImageView>(R.id.ivHeader)
        Glide.with(mContext).load(item.user?.avatar_url).into(ivHeader!!)

        val ivLike = holder.getView<ImageView>(R.id.ivLike)
        val ivShare = holder.getView<ImageView>(R.id.ivShare)
        val ivDownload = holder.getView<ImageView>(R.id.ivDownload)
        val ivMore = holder.getView<ImageView>(R.id.ivMore)


        gifImageView?.setOnClickListener {

        }

        ivPlay?.setOnClickListener {
            ivPlay.visibility = GONE
            if (FileUtils.checkExist(tempPath)) {
                progressLoading?.visibility = GONE
                ivRetry?.visibility = GONE
                drawable?.start()
            } else {
                download(
                    fileName,
                    gifUrl,
                    cachePath,
                    tempPath,
                    gifImageView,
                    tvTempPath,
                    tvStatus,
                    progressBarDebug,
                    progressLoading, ivPlay, ivRetry
                )
            }
        }

        ivRetry?.setOnClickListener {
            ToastUtils.show("重新下载")
            download(
                fileName,
                gifUrl,
                cachePath,
                tempPath,
                gifImageView,
                tvTempPath,
                tvStatus,
                progressBarDebug,
                progressLoading, ivPlay, ivRetry
            )
        }

        ivLike?.setOnClickListener {
            ToastUtils.show("like")
        }

        ivShare?.setOnClickListener {
            ToastUtils.show("share")
        }

        ivDownload?.setOnClickListener {
            ToastUtils.show("保存")
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
            drawable = GifDrawable(tempPath)
            gifImageView?.setImageDrawable(drawable)

            progressBarDebug?.visibility = GONE
            ivPlay?.visibility = GONE
            ivRetry?.visibility = GONE

            return
        }

        download(
            fileName,
            gifUrl,
            cachePath,
            tempPath,
            gifImageView,
            tvTempPath,
            tvStatus,
            progressBarDebug,
            progressLoading,
            ivPlay,
            ivRetry
        )
    }

    private fun download(
        fileName: String,
        gifUrl: String,
        cachePath: String,
        tempPath: String,
        gifImageView: GifImageView?,
        tvTempPath: TextView?,
        tvStatus: TextView?,
        progressBar: ProgressBar?,
        progressLoading: ProgressBar?, ivPlay: View?, ivRetry: View?
    ) {
        val task = TaskInfo(fileName, Global.cacheDir, gifUrl)
        tvTempPath?.text = "临时路径：$cachePath"

        DownloadManager.getInstance().start(task, object : DownloadCallback {
            override fun onStart() {
                tvStatus?.text = "状态：开始下载"
                progressLoading?.visibility = VISIBLE
                ivPlay?.visibility = GONE
                ivRetry?.visibility = GONE
            }

            override fun onConnected(totalLength: Long) {
                tvStatus?.text = "状态：已连接"
                progressBar?.max = totalLength.toInt()
            }

            override fun onProgress(current: Long, totalLength: Long) {
                progressBar?.progress = current.toInt()
                tvStatus?.text = "状态：下载中: $current"
            }

            override fun onPause(taskInfo: TaskInfo?) {
                tvStatus?.text = "状态：暂停下载"

                progressLoading?.visibility = GONE
                ivPlay?.visibility = GONE
                ivRetry?.visibility = VISIBLE

            }

            override fun onCompleted(taskInfo: TaskInfo?) {
                tvStatus?.text = "状态：下载完成"
                FileUtil.createNewFile(tempPath, false)
                com.android.absbase.utils.FileUtils.copyFile(cachePath, tempPath, true)
                val drawable = GifDrawable(tempPath)
                gifImageView?.setImageDrawable(drawable)
                progressLoading?.visibility = GONE
                ivPlay?.visibility = GONE
                ivRetry?.visibility = GONE
            }

            override fun onError(e: Exception?) {
                tvStatus?.text = "状态：下载出错"
                progressLoading?.visibility = GONE
                ivPlay?.visibility = GONE
                ivRetry?.visibility = VISIBLE
            }

        }, true)
    }

    companion object {
        private const val STATUS_PLAY = 0
        private const val STATUS_PAUSE = 1
        private const val STATUS_RETRY = 2
        private const val STATUS_HIDE = 4
    }
}