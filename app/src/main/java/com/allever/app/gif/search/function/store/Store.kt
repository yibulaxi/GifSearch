package com.allever.app.gif.search.function.store

import com.xm.lib.datastroe.DataStore

object Store {

    fun saveToken(token: String) {
        DataStore.putString(CacheKey.TOKEN, token)
    }

    fun getToken(): String = DataStore.getString(CacheKey.TOKEN)

    fun saveUserId(userId: Int) {
        DataStore.putInt(CacheKey.USER_ID, userId)
    }

    fun getUserId(): Int = DataStore.getInt(CacheKey.USER_ID)

    fun saveVersion(version: Int) {
        DataStore.putInt("version", version)
    }

    fun getVersion(): Int = DataStore.getInt("version", Version.INTERNAL)
}