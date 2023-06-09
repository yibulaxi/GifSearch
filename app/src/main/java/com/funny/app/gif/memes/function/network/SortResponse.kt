package com.funny.app.gif.memes.function.network

/**
 * 项目分类
 */
data class ProjectSortData(
    val children: List<Any>,
    val courseId: Int,
    val id: Int,
    val name: String,
    val order: Int,
    val parentChapterId: Int,
    val userControlSetTop: Boolean,
    val visible: Int
)