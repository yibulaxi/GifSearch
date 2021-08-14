package com.allever.app.gif.search.function.network

import com.allever.app.gif.search.bean.TrendingResponse
import com.allever.app.gif.search.function.network.response.*
import com.allever.app.gif.search.function.network.response.giffun.*
import com.allever.app.gif.search.giffun.AuthUtil
import com.allever.app.gif.search.util.Utility
import com.xm.lib.util.log
import com.xm.lib.util.loge

object NetRepository {

    private val apiService by lazy {
        ServiceCreator.create(Api::class.java)
    }

    private val gifFunApiService by lazy {
        GifFunServiceCreator.create(GifFunApi::class.java)
    }

    suspend fun initGifFun(token: String, userId: String): InitResponse {
        val param = listOf(userId, token)
        return gifFunApiService.init(param[0], param[1], AuthUtil.getServerVerifyCode(*param.toTypedArray()))
    }

    suspend fun fetchWorld(token: String, userId: String, lastFeed: String, failureTask: (errorMsg: String) -> Unit = {}): BaseResponse<FetchWorldResponse> {
        val param = listOf(userId, token)
        return getGifFunData(failureTask, "获取Gif列表成功") {
            if (lastFeed.isEmpty() || lastFeed == "0") {
                gifFunApiService.fetchWorld(param[0], param[1], AuthUtil.getServerVerifyCode(*param.toTypedArray()))
            } else {
                gifFunApiService.fetchWorldWithLastFeed(param[0], param[1], AuthUtil.getServerVerifyCode(*param.toTypedArray()), lastFeed)
            }
        }
    }

    suspend fun getAuthorizedUrl(token: String, userId: String, feedId: String, url: String,  failureTask: (errorMsg: String) -> Unit = {}): BaseResponse<AuthorizeUrlResponse> {
        val param = listOf(feedId, userId, token)
        return getGifFunData(failureTask, "获取GifUrl成功") {
            gifFunApiService.getAuthorizeUrl(userId, token, feedId, url, AuthUtil.getServerVerifyCode(*param.toTypedArray()))
        }
    }

    suspend fun fetchVCode(phone: String): VCodeResponse {
        val param = listOf(Utility.deviceName, phone, Utility.getDeviceSerial())
        val response = gifFunApiService.fetchVCode(number = param[1], params = AuthUtil.getServerVerifyCode(*param.toTypedArray()))
        return response
    }

    suspend fun login(phone: String, code: String) : LoginResponse {
        val response = gifFunApiService.login(phone, code)
        return response
    }

    suspend fun register(phone: String, code: String, nickname: String) : RegisterResponse {
        val response = gifFunApiService.register(phone, code, nickname)
        return response
    }

    suspend fun getTrendList(
        pageNum: Int,
        failureTask: (errorMsg: String) -> Unit = {}
    ): BaseResponse<TrendingResponse> {
        return getGiphyData(failureTask, "获取Gif列表成功") {
            apiService.trendingGif(offset = pageNum.toString(), limit = 10.toString())
        }
    }

    suspend fun <T> getGifFunData(
        failureTask: (errorMsg: String) -> Unit,
        successMsg: String = "获取数据成功",
        block: suspend () ->  GifFunResponse
    ): BaseResponse<T> {
        val response = BaseResponse<T>()
        try {
            val data = block()
            response.errorCode = data.status
            response.errorMsg = data.msg
            if (data.status == 0) {
                response.data = data as T
                log(successMsg)
            } else {
                failureTask("${data.javaClass.simpleName}: ${data.status} -> ${data.msg}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            response.errorCode = -1
            response.errorMsg = e.message ?: ""
            failureTask(response.errorMsg)
        }
        return response
    }

    suspend fun <T> getGiphyData(
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