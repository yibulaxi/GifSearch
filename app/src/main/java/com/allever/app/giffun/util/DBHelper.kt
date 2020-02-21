package com.allever.app.giffun.util

import com.allever.app.giffun.bean.DataBean
import com.allever.app.giffun.bean.LikedItem
import com.google.gson.Gson
import org.litepal.LitePal

object DBHelper {

    fun getLikedList(): MutableList<DataBean> {
        val list = LitePal.findAll(LikedItem::class.java)
        val result = mutableListOf<DataBean>()
        val gson = Gson()
        list.map {
            val dataBean = gson.fromJson(it.data, DataBean::class.java)
            result.add(dataBean)
        }
        return result
    }

    fun liked(gifId: String, dataBean: DataBean) {
        val data = LitePal.where("gifId = ?", gifId).find<LikedItem>(LikedItem::class.java)
        if (data.isEmpty()) {
            val newLike = LikedItem()
            newLike.gifId = gifId
            newLike.data = Gson().toJson(dataBean)
            newLike.save()
        }
    }

    fun unLiked(gifId: String) {
        val data = LitePal.where("gifId = ?", gifId).find<LikedItem>(LikedItem::class.java)
        data.map {
            it.delete()
        }
    }

    fun isLiked(gifId: String): Boolean {
        val data = LitePal.where("gifId = ?", gifId).find<LikedItem>(LikedItem::class.java)
        return data.isNotEmpty()
    }
}