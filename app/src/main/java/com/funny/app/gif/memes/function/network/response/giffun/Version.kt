/*
 * Copyright (C) guolin, Suzhou Quxiang Inc. Open source codes for study only.
 * Do not use for commercial purpose.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.funny.app.gif.memes.function.network.response.giffun

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * 版本更新的实体类封装，如果存在版本更新则会提供下述信息。
 * @author guolin
 * @since 2018/7/8
 */
@Parcelize
class Version(@SerializedName("change_log") val changeLog: String,
              @SerializedName("is_force") val isForce: Boolean,
              val url: String,
              @SerializedName("version_name") val versionName: String,
              @SerializedName("version_code") val versionCode: Int,
              val channel: String) : Parcelable