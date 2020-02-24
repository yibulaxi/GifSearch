package com.allever.app.giffun.ui.mvp.model

import com.allever.app.giffun.bean.RandomResponse
import com.allever.app.giffun.bean.SearchResponse
import com.allever.app.giffun.bean.TranslateResponse
import com.allever.app.giffun.bean.TrendingResponse
import retrofit2.http.GET
import retrofit2.http.Query
import rx.Observable

/**
 * Created by Allever on 2017/1/15.
 */

interface RetrofitService {

    private val API_KEY: String
        get() = "gdsqQrQq2mEOPm7Q5ZQ2jHRNX91kwYG8"

    //https://api.giphy.com/v1/gifs/random?api_key=gdsqQrQq2mEOPm7Q5ZQ2jHRNX91kwYG8
    @GET("gifs/random?")
    fun getRandomGif(
        @Query("api_key") api_key: String = API_KEY
    ): Observable<RandomResponse>

    //https://api.giphy.com/v1/gifs/random?api_key=gdsqQrQq2mEOPm7Q5ZQ2jHRNX91kwYG8
    @GET("stickers/random?")
    fun getRandomSticker(
        @Query("api_key") api_key: String = API_KEY
    ): Observable<RandomResponse>


    //https://api.giphy.com/v1/gifs/search?api_key=gdsqQrQq2mEOPm7Q5ZQ2jHRNX91kwYG8&q=cat&offset=0&limit=1
    @GET("gifs/search?")
    fun searchGif(
        @Query("q") q: String,
        @Query("api_key") api_key: String = API_KEY,
        @Query("offset") offset: String = "0",
        @Query("limit") limit: String = "10"
    ): Observable<SearchResponse>


    //https://api.giphy.com/v1/gifs/trending?api_key=gdsqQrQq2mEOPm7Q5ZQ2jHRNX91kwYG8&q=cat&offset=0&limit=10
    @GET("gifs/trending?")
    fun trendingGif(
        @Query("api_key") api_key: String = API_KEY,
        @Query("offset") offset: String = "0",
        @Query("limit") limit: String = "10"
    ): Observable<TrendingResponse>


    //https://api.giphy.com/v1/gifs/translate?api_key=gdsqQrQq2mEOPm7Q5ZQ2jHRNX91kwYG8&s=cat
    @GET("gifs/translate?")
    fun translateGif(
        @Query("s") s: String,
        @Query("api_key") api_key: String = API_KEY
    ): Observable<TranslateResponse>


//    @GET("translate_a/single?client=gtx&dt=t&dt=bd&dt=rm&dj=1&ie=UTF-8&oe=UTF-8&sl=auto&tl=zh-CN&hl=zh-CN&tk=&q=")
//    fun translate(@Query("q") content: String): Observable<TranslationBean>

    /*
    @GET("translate_a/single")
    fun translate(
        @Query("q") q: String,
        @Query("client") content: String = "gtx",
        @Query("dt") dt: String = "t",
        @Query("dt") dt1: String = "bd",
        @Query("dt") dt2: String = "rm",
        @Query("dj") dj: String = "1",
        @Query("ie") ie: String = "UTF-8",
        @Query("oe") oe: String = "UTF-8",
        @Query("sl") sl: String = "auto",
        @Query("tl") tl: String = "en",
        @Query("hl") hl: String = "zh-CN",
        @Query("tk") tk: String = ""

    ): Observable<TranslationBean>

    //https://translate.google.cn/translate_tts?client=gtx&ie=UTF-8&tl=zh-CN&total=1&idx=0&textlen=2&tk=&q=设置
    @GET("translate_tts")
    fun requestTTS(
        @Query("q") q: String,
        @Query("client") content: String = "gtx",
        @Query("ie") ie: String = "UTF-8",
        @Query("tl") tl: String = "en",
        @Query("hl") hl: String = "zh-CN",
        @Query("total") total: String = "1",
        @Query("idx") idx: String = "0",
        @Query("textlen") textlen: String = "0",
        @Query("tk") tk: String = ""
    ): Call<ResponseBody>

     */

}
