package com.allever.app.gif.search.function.network

import com.allever.app.gif.search.bean.TrendingResponse
import com.allever.app.gif.search.function.network.response.BannerData
import com.allever.app.gif.search.function.network.response.PageData
import com.xm.lib.util.log
import com.xm.lib.util.loge

object NetRepository {

    private val apiService by lazy {
        ServiceCreator.create(Api::class.java)
    }

    suspend fun getTrendList(
        pageNum: Int,
        failureTask: (errorMsg: String) -> Unit
    ): BaseResponse<TrendingResponse> {
        return getData2(failureTask, "获取Gif列表成功") {
            apiService.trendingGif(offset = pageNum.toString(), limit = 10.toString())
        }
    }

    suspend fun <T> getData2(
        failureTask: (errorMsg: String) -> Unit,
        successMsg: String = "获取数据成功",
        block: suspend () -> T
    ): BaseResponse<T> {
        val response = BaseResponse<T>()
        try {
            val data = block()
            response.data = data
            response.errorMsg = ""
            response.errorCode = 0
            log(successMsg)
        } catch (e: Exception) {
            e.printStackTrace()
            response.errorMsg = e.message ?: ""
            failureTask(response.errorMsg)
        }
        return response
    }

    suspend fun getHomePageList(pageNum: Int, failureTask: (errorMsg: String) -> Unit): PageData? {
        return getData(failureTask, "获取首页文章列表成功") {
            apiService.getHomePageList(pageNum)
        }
    }

    suspend fun getProjectPageList(
        cid: Int,
        pageNum: Int,
        failureTask: (errorMsg: String) -> Unit
    ): PageData? {
        return getData(failureTask, "获取项目文章列表成功") {
            apiService.getProjectPageList(cid, pageNum)
        }
    }

    suspend fun getBanner(failureTask: (errorMsg: String) -> Unit): List<BannerData>? {
        return getData(failureTask, "获取首页banner成功") {
            apiService.getBanner()
        }
    }

    suspend fun getProjectSortData(failureTask: (errorMsg: String) -> Unit): List<ProjectSortData>? {
        return getData(failureTask, "获取项目分类成功") {
            apiService.getProjectSort()
        }
    }

    private suspend fun <T> getData(
        failureTask: (errorMsg: String) -> Unit,
        successMsg: String = "获取数据成功",
        block: suspend () -> BaseResponse<T>?
    ): T? {
        return try {
            val baseResponse = block()
            val result = getResponseData(baseResponse) {
                failureTask(it)
            }
            if (result != null) {
                log(successMsg)
            }
            return result
        } catch (e: Exception) {
            //失败
            e.printStackTrace()
            failureTask("${e.message}")
            null
        }
    }

    suspend fun getSortData3(failureTask: (errorMsg: String) -> Unit): List<ProjectSortData>? {
        return try {
            val baseResponse = apiService.getSortData3()
            val result = getResponseData(baseResponse) {
                failureTask(it)
            }
            if (result != null) {
                log("获取分类成功")
            }
            return result
        } catch (e: Exception) {
            //失败
            e.printStackTrace()
            failureTask("${e.message}")
            null
        }
    }

    private fun <T> getResponseData(baseResponse: BaseResponse<T>?): T? {
        baseResponse?.data ?: return null
        return if (baseResponse.errorCode != 0) {
            //成功-数据异常
            loge(baseResponse.errorMsg)
            null
        } else {
            //成功
            baseResponse.data
        }
    }

    private fun <T> getResponseData(
        baseResponse: BaseResponse<T>?,
        failureTask: (errorMsg: String) -> Unit
    ): T? {
        baseResponse?.data ?: return null
        return if (baseResponse.errorCode != 0) {
            //成功-数据异常
//            loge(baseResponse.errorMsg)
            failureTask(baseResponse.errorMsg)
            null
        } else {
            //成功
            baseResponse.data
        }
    }
}