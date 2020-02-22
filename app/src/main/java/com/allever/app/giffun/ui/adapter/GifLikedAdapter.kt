package com.allever.app.giffun.ui.adapter

import android.content.Context
import android.widget.ImageView
import com.allever.app.giffun.R
import com.allever.app.giffun.app.Global
import com.allever.app.giffun.bean.DataBean
import com.allever.app.giffun.util.MD5
import com.allever.lib.common.ui.widget.recycler.BaseRecyclerViewAdapter
import com.allever.lib.common.ui.widget.recycler.BaseViewHolder
import com.allever.lib.common.util.FileUtils
import com.allever.lib.common.util.log
import com.bumptech.glide.Glide
import java.io.File

class GifLikedAdapter(context: Context, resId: Int, data: MutableList<DataBean>) :
    BaseRecyclerViewAdapter<DataBean>(context, resId, data) {
    override fun bindHolder(holder: BaseViewHolder, position: Int, item: DataBean) {
        val fileName = MD5.getMD5Str(item.id) + ".gif"
        val tempPath = "${Global.tempDir}${File.separator}$fileName"
        val url = if (FileUtils.checkExist(tempPath)) {
            tempPath
        } else {
            item.images.fixed_height.url
        }

        log("LikedAdapter: url = $url")
        val ivGif = holder.getView<ImageView>(R.id.ivGif)
        Glide.with(mContext).asBitmap().load(url).into(ivGif!!)
    }
}