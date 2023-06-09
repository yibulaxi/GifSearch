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
 * 注册请求所使用的通用基类。
 * @author guolin
 * @since 2018/10/31
 */
open class RegisterResponse : GifFunResponse() {

    /**
     * 用户的账号id。
     */
    @SerializedName("user_id")
    var userId: Int = 0

    /**
     * 记录用户的登录身份，token有效期30天。
     */
    var token: String = ""

    /**
     * 用户在第三方账号上所使用的头像，如果账号未注册时会返回此参数。
     */
    var avatar: String = ""

}