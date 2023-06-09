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
 * 获取用户所发Feeds请求的实体类封装。
 *
 * @author guolin
 * @since 17/7/23
 */
class FetchUserFeedsResponse : GifFunResponse() {

    @SerializedName("user_id")
    var userId: Long = 0

    var nickname: String = ""

    var avatar: String = ""

    @SerializedName("bg_image")
    var bgImage: String = ""

    @SerializedName("feeds_count")
    var feedsCount: Int = 0

    @SerializedName("followings_count")
    var followingsCount: Int = 0

    @SerializedName("followers_count")
    var followersCount: Int = 0

    @SerializedName("is_following")
    var isFollowing: Boolean = false

    var description: String = ""

    @SerializedName("data")
    var feeds: MutableList<UserFeed> = ArrayList()

}