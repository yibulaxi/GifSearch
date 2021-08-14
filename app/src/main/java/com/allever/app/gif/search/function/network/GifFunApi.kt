package com.allever.app.gif.search.function.network

import com.allever.app.gif.search.function.network.response.giffun.*
import com.allever.app.gif.search.giffun.GlobalUtil
import com.allever.app.gif.search.giffun.NetworkConst
import com.allever.app.gif.search.util.Utility
import com.xm.netmodel.impl.HttpRequestImpl
import retrofit2.http.*

interface GifFunApi {

    companion object {
        private val api by lazy {
            HttpRequestImpl.getRequest().createApi(GifFunApi::class.java)
        }

        fun getApi(vararg classes: Class<*>?): GifFunApi = if (classes.isEmpty()) {
            api
        } else {
            HttpRequestImpl.getRequest().createApi(GifFunApi::class.java, *classes)
        }
    }


    /**
    verride fun params(): Map<String, String>? {
    val params = HashMap<String, String>()
    params[NetworkConst.CLIENT_VERSION] = GlobalUtil.appVersionCode.toString()
    val appChannel =  GlobalUtil.getApplicationMetaData("APP_CHANNEL")
    if (appChannel != null) {
    params[NetworkConst.CLIENT_CHANNEL] = appChannel
    }
    if (buildAuthParams(params)) {
    params[NetworkConst.DEVICE_NAME] = deviceName
    }
    return params
    }

    override fun headers(builder: Headers.Builder): Headers.Builder {
    buildAuthHeaders(builder, NetworkConst.UID, NetworkConst.TOKEN)
    return super.headers(builder)
    }
     */
    @GET("/init")
    suspend fun init(
        @Query(NetworkConst.UID) userId: String,
        @Query(NetworkConst.TOKEN) token: String,
        @Header(NetworkConst.VERIFY) params: String,
        @Header(NetworkConst.HEADER_USER_AGENT) agent: String = NetworkConst.HEADER_USER_AGENT_VALUE,
        @Header(NetworkConst.HEADER_APP_VERSION ) appVersion: String = Utility.appVersion,
        @Header(NetworkConst.HEADER_APP_SIGN) sign: String = Utility.appSign,
        @Query(NetworkConst.DEVICE_NAME) deviceName: String = Utility.deviceName,
        @Query(NetworkConst.DEVICE_SERIAL) serial: String = Utility.getDeviceSerial(),
        @Query(NetworkConst.CLIENT_VERSION) clientVersion: String = GlobalUtil.appVersionCode.toString()
    ): InitResponse

    @GET("/feeds/world")
    suspend fun fetchWorld(
        @Query(NetworkConst.UID) userId: String,
        @Query(NetworkConst.TOKEN) token: String,
        @Header(NetworkConst.VERIFY) params: String,
        @Header(NetworkConst.HEADER_USER_AGENT) agent: String = NetworkConst.HEADER_USER_AGENT_VALUE,
        @Header(NetworkConst.HEADER_APP_VERSION ) appVersion: String = Utility.appVersion,
        @Header(NetworkConst.HEADER_APP_SIGN) sign: String = Utility.appSign,
        @Query(NetworkConst.DEVICE_NAME) deviceName: String = Utility.deviceName,
        @Query(NetworkConst.DEVICE_SERIAL) serial: String = Utility.getDeviceSerial(),
        @Query(NetworkConst.CLIENT_VERSION) clientVersion: String = GlobalUtil.appVersionCode.toString()
    ): FetchWorldResponse

    @GET("/feeds/world")
    suspend fun fetchWorldWithLastFeed(
        @Query(NetworkConst.UID) userId: String,
        @Query(NetworkConst.TOKEN) token: String,
        @Header(NetworkConst.VERIFY) params: String,
        @Query(NetworkConst.LAST_FEED) lastFeed: String,
        @Header(NetworkConst.HEADER_USER_AGENT) agent: String = NetworkConst.HEADER_USER_AGENT_VALUE,
        @Header(NetworkConst.HEADER_APP_VERSION ) appVersion: String = Utility.appVersion,
        @Header(NetworkConst.HEADER_APP_SIGN) sign: String = Utility.appSign,
        @Query(NetworkConst.DEVICE_NAME) deviceName: String = Utility.deviceName,
        @Query(NetworkConst.DEVICE_SERIAL) serial: String = Utility.getDeviceSerial(),
        @Query(NetworkConst.CLIENT_VERSION) clientVersion: String = GlobalUtil.appVersionCode.toString()
    ): FetchWorldResponse

    /**
     *
     * builder.add(NetworkConst.HEADER_USER_AGENT, NetworkConst.HEADER_USER_AGENT_VALUE)
    builder.add(NetworkConst.HEADER_APP_VERSION, Utility.appVersion)
    builder.add(NetworkConst.HEADER_APP_SIGN, Utility.appSign)

     * params[NetworkConst.NUMBER] = number
    params[NetworkConst.DEVICE_NAME] = deviceName
    params[NetworkConst.DEVICE_SERIAL] = deviceSerial
     */
    @FormUrlEncoded
    @POST("/login/fetch_verify_code")
    suspend fun fetchVCode(
        @Header(NetworkConst.VERIFY) params: String,
        @Header(NetworkConst.HEADER_USER_AGENT) agent: String = NetworkConst.HEADER_USER_AGENT_VALUE,
        @Header(NetworkConst.HEADER_APP_VERSION ) appVersion: String = Utility.appVersion,
        @Header(NetworkConst.HEADER_APP_SIGN) sign: String = Utility.appSign,
        @Field(NetworkConst.NUMBER) number: String,
        @Field(NetworkConst.DEVICE_SERIAL) serial: String = Utility.getDeviceSerial(),
        @Field(NetworkConst.DEVICE_NAME) deviceName : String= Utility.deviceName
    ): VCodeResponse


    /**
    params[NetworkConst.NUMBER] = number
    params[NetworkConst.CODE] = code
    params[NetworkConst.DEVICE_NAME] = deviceName
    params[NetworkConst.DEVICE_SERIAL] = deviceSerial
     */
    @FormUrlEncoded
    @POST("/login/phone")
    suspend fun login(
        @Field(NetworkConst.NUMBER) number: String,
        @Field(NetworkConst.CODE) code: String,
        @Field(NetworkConst.DEVICE_SERIAL) serial: String = Utility.getDeviceSerial(),
        @Field(NetworkConst.DEVICE_NAME) deviceName : String= Utility.deviceName
    ): LoginResponse

    /**
    params[NetworkConst.NUMBER] = number
    params[NetworkConst.CODE] = code
    params[NetworkConst.NICKNAME] = nickname
    params[NetworkConst.DEVICE_NAME] = deviceName
    params[NetworkConst.DEVICE_SERIAL] = deviceSerial
     */
    @FormUrlEncoded
    @POST("/register/phone")
    suspend fun register(
        @Field(NetworkConst.NUMBER) number: String,
        @Field(NetworkConst.CODE) code: String,
        @Field(NetworkConst.NICKNAME) nickname: String,
        @Field(NetworkConst.DEVICE_SERIAL) serial: String = Utility.getDeviceSerial(),
        @Field(NetworkConst.DEVICE_NAME) deviceName : String= Utility.deviceName
    ): RegisterResponse

}