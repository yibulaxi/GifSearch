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

import com.google.gson.annotations.SerializedName

/**
 * 获取私有空间图片下载地址的实体类封装。
 *
 * @author guolin
 * @since 17/6/24
 */
class AuthorizeUrlResponse : GifFunResponse() {

    /**
     * GIF图片的真实可访问url地址。
     */
    @SerializedName("authorize_url")
    var authorizeUrl: String = ""


}