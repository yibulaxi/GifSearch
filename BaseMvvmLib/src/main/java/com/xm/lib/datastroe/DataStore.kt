package com.xm.lib.datastroe

import android.content.Context
import android.os.Parcelable
import com.tencent.mmkv.MMKV
import com.xm.lib.util.CoroutineHelper
import kotlinx.coroutines.launch

/**
 * 数据存储类
 */
object DataStore : IDataStore {

    private lateinit var mmkv: MMKV

    /**
     * 初始化
     */
    fun init(context: Context) {
        MMKV.initialize(context)
        mmkv = MMKV.defaultMMKV()
    }

    override fun putInt(key: String, value: Int) {
        mmkv.encode(key, value)
    }

    override fun getInt(key: String) = mmkv.decodeInt(key)
    override fun getInt(key: String, default: Int) = mmkv.decodeInt(key, default)

    override fun putLong(key: String, value: Long) {
        mmkv.encode(key, value)
    }

    override fun getLong(key: String): Long = mmkv.decodeLong(key)
    override fun getLong(key: String, default: Long): Long = mmkv.decodeLong(key, default)

    override fun putFloat(key: String, value: Float) {
        mmkv.encode(key, value)
    }

    override fun getFloat(key: String) = mmkv.decodeFloat(key)
    override fun getFloat(key: String, default: Float): Float = mmkv.decodeFloat(key, default)

    override fun putDouble(key: String, value: Double) {
        mmkv.encode(key, value)
    }

    override fun getDouble(key: String) = mmkv.decodeDouble(key)
    override fun getDouble(key: String, default: Double): Double = mmkv.decodeDouble(key, default)
    override fun putString(key: String, value: String) {
        mmkv.encode(key, value)
    }

    override fun getString(key: String) = mmkv.decodeString(key) ?: ""
    override fun getString(key: String, default: String): String =
        mmkv.decodeString(key, default) ?: ""

    override fun putParcelable(key: String, value: Parcelable) {
        mmkv.encode(key, value)
    }

    override fun <T : Parcelable> getParcelable(key: String, clz: Class<T>): T? {
        return mmkv.decodeParcelable(key, clz)
    }
}