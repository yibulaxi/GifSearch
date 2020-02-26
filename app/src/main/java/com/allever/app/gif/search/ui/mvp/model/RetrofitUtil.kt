package com.allever.app.gif.search.ui.mvp.model


import com.allever.app.gif.search.bean.RandomResponse
import com.allever.app.gif.search.bean.SearchResponse
import com.allever.app.gif.search.bean.TranslateResponse
import com.allever.app.gif.search.bean.TrendingResponse
import java.util.concurrent.TimeUnit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by Allever on 2017/1/15.
 */

object RetrofitUtil {
    private val BASE_URL = "https://api.giphy.com/v1/"
    private val retrofit: Retrofit
    private val retrofitService: RetrofitService

    init {
        val client = OkHttpClient.Builder()
            .connectTimeout(8000, TimeUnit.SECONDS)
            .build()
        retrofit = Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .baseUrl(BASE_URL)
            .build()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    fun getRandomGif(subscriber: Subscriber<RandomResponse>) {
        retrofitService.getRandomGif()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .unsubscribeOn(Schedulers.io())
            .subscribe(subscriber)
    }

    fun searchGif(q: String, offset: Int, count: Int, subscriber: Subscriber<SearchResponse>) {
        retrofitService.searchGif(q = q, offset = offset.toString(), limit = count.toString())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .unsubscribeOn(Schedulers.io())
            .subscribe(subscriber)
    }

    fun trendingGif(offset: Int, count: Int, subscriber: Subscriber<TrendingResponse>) {
        retrofitService.trendingGif(offset = offset.toString(), limit = count.toString())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .unsubscribeOn(Schedulers.io())
            .subscribe(subscriber)
    }


    fun translateGif(s: String, subscriber: Subscriber<TranslateResponse>) {
        retrofitService.translateGif(s = s)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .unsubscribeOn(Schedulers.io())
            .subscribe(subscriber)
    }

    /*
    fun translate(
        subscriber: Subscriber<TranslationBean>,
        content: String,
        sl: String,
        tl: String
    ) {
        retrofitService.translate(content, sl = sl, tl = tl)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .unsubscribeOn(Schedulers.io())
            .subscribe(subscriber)
    }

    fun requestTTS(content: String, tl: String, callback: Callback<ResponseBody>) {
        val call =
            retrofitService.requestTTS(q = content, tl = tl, textlen = content.length.toString())
        call.enqueue(callback)
    }

     */
}
