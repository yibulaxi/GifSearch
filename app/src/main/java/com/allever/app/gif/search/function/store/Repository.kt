package com.allever.app.gif.search.function.store

import com.allever.app.gif.search.app.Global
import com.allever.app.gif.search.function.network.NetRepository
import com.allever.app.gif.search.ui.adapter.bean.GifItem
import com.allever.lib.common.util.log

object Repository {

    suspend fun getGifItemList(last: String): MutableList<GifItem> {
        val gifItemList = mutableListOf<GifItem>()
        if (Store.getVersion() == Version.INTERNAL) {
            //国内版本
            val response = NetRepository.fetchWorld(Store.getToken(), Store.getUserId().toString(), last)

            response.let {
                it.data?.feeds?.let {
                    it.map {
                        log("feedId: ${it.feedId} -> ${it.gif}")
                        val gifItem = GifItem()
                        gifItem.id = it.feedId.toString()
                        gifItem.type = Version.INTERNAL
                        gifItem.avatar = it.avatar
                        gifItem.nickname = it.nickname
                        gifItem.title = it.content
                        gifItem.size = it.fsize
                        gifItem.tempUrl = it.gif
                        gifItem.cover = it.cover
                        gifItemList.add(gifItem)
                    }
                }
            }
        } else {
            //国外版本
            val response = NetRepository.getTrendList(last.toInt(), Global.SHOW_COUNT)
            //成功
            response.data?.let {
                val data = it.data
                data?.map {
                    log("trending = ${it.images.original.url}")
                    val gifItem = GifItem()
                    gifItem.id = it.id
                    gifItem.type = Version.INTERNATIONAL
                    gifItem.avatar = it?.user?.avatar_url ?: ""
                    gifItem.nickname = it?.user?.display_name ?: ""
                    gifItem.title = it.title ?: ""
                    gifItem.size = it.images.fixed_height.size.toLong()
                    gifItem.url = it.images.fixed_height.url
                    gifItemList.add(gifItem)
                }
            }
        }
        return gifItemList
    }

    suspend fun search(keyword: String, last: String): MutableList<GifItem> {
        val gifItemList = mutableListOf<GifItem>()
        if (Store.getVersion() == Version.INTERNAL) {
            //国内版本
            val response = NetRepository.search(keyword, Store.getToken(), Store.getUserId().toString(), last)
            response.let {
                it.data?.data?.let {
                    it.map {
                        log("feedId: ${it.feedId} -> ${it.gif}")
                        val gifItem = GifItem()
                        gifItem.id = it.feedId.toString()
                        gifItem.type = Version.INTERNAL
                        gifItem.avatar = it.avatar
                        gifItem.nickname = it.nickname
                        gifItem.title = it.content
                        gifItem.size = it.fsize
                        gifItem.tempUrl = it.gif
                        gifItem.cover = it.cover
                        gifItemList.add(gifItem)
                    }
                }
            }
        } else {
            val response = NetRepository.search(keyword, last.toInt(), Global.SHOW_COUNT)
            response.data?.let {
                val data = it.data
                data?.map {
                    log("trending = ${it.images.original.url}")
                    val gifItem = GifItem()
                    gifItem.id = it.id
                    gifItem.type = Version.INTERNATIONAL
                    gifItem.avatar = it?.user?.avatar_url ?: ""
                    gifItem.nickname = it?.user?.display_name ?: ""
                    gifItem.title = it.title ?: ""
                    gifItem.size = it.images.fixed_height.size.toLong()
                    gifItem.url = it.images.fixed_height.url
                    gifItemList.add(gifItem)
                }
            }
        }

        return gifItemList
    }
}