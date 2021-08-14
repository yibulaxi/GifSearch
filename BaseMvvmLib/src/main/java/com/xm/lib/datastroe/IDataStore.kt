package com.xm.lib.datastroe

import android.os.Parcelable

interface IDataStore {

    fun putInt(key: String, value: Int)
    fun getInt(key: String) : Int
    fun getInt(key: String, default: Int) : Int

    fun putLong(key: String, value: Long)
    fun getLong(key: String) : Long
    fun getLong(key: String, default: Long) : Long

    fun putFloat(key: String, value: Float)
    fun getFloat(key: String) : Float
    fun getFloat(key: String, default: Float) : Float

    fun putDouble(key: String, value: Double)
    fun getDouble(key: String) : Double
    fun getDouble(key: String, default: Double) : Double

    fun putString(key: String, value: String)
    fun getString(key: String) : String
    fun getString(key: String, default: String) : String

    fun putParcelable(key: String, value : Parcelable)
    fun <T: Parcelable> getParcelable(key: String, clz: Class<T>): T?

}