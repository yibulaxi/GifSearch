package com.funny.app.gif.memes.function.media

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import com.funny.lib.common.app.App
import com.funny.lib.common.util.log
import com.android.absbase.utils.FileUtils.isExistFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

object MediaHelper {

    const val TYPE_IMAGE = "TYPE_IMAGE"
    const val TYPE_VIDEO = "TYPE_VIDEO"

    private val COLUMN_BUCKET_ID = "bucket_id"
    private val COLUMN_BUCKET_DISPLAY_NAME = "bucket_display_name"
    private val COLUMN_DATE_TAKEN = "datetaken"
    private val COLUMN_DATA = "_data"
    private const val COLUMN_URI = "uri"
    private const val COLUMN_COUNT = "count"
    private val QUERY_URI = MediaStore.Files.getContentUri("external")

    private val COLUMNS = arrayOf(
        MediaStore.Files.FileColumns._ID,
        COLUMN_BUCKET_ID,
        COLUMN_BUCKET_DISPLAY_NAME,
        MediaStore.MediaColumns.MIME_TYPE,
        COLUMN_URI,
        COLUMN_COUNT
    )

    private val PROJECTION_29 = arrayOf(
        MediaStore.Files.FileColumns._ID,
        COLUMN_BUCKET_ID,
        COLUMN_BUCKET_DISPLAY_NAME,
        COLUMN_DATE_TAKEN,
        COLUMN_DATA,
        MediaStore.MediaColumns.MIME_TYPE
    )

    // === params for showSingleMediaType: false ===
    private val SELECTION = (
            "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0"
                    + ") GROUP BY (bucket_id")
    private val SELECTION_29 = (
            "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0")
    private val SELECTION_ARGS = arrayOf(
        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
    )

    private val SELECTION_ARGS_IMAGE = arrayOf(
        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString()
    )

    private val SELECTION_ARGS_VIDEO = arrayOf(
        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
    )
    // =============================================

    // === params for showSingleMediaType: true ===
    private val SELECTION_FOR_SINGLE_MEDIA_TYPE = (
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0"
                    + ") GROUP BY (bucket_id")
    private val SELECTION_FOR_SINGLE_MEDIA_TYPE_29 = (
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0")

    private fun getSelectionArgsForSingleMediaType(mediaType: Int): Array<String> {
        return arrayOf(mediaType.toString())
    }
    // =============================================

    // === params for showSingleMediaType: true ===
    private val SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE = (
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0"
                    + " AND " + MediaStore.MediaColumns.MIME_TYPE + "=?"
                    + ") GROUP BY (bucket_id")
    private val SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE_29 = (
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0"
                    + " AND " + MediaStore.MediaColumns.MIME_TYPE + "=?")

    private fun getSelectionArgsForSingleMediaGifType(mediaType: Int): Array<String> {
        return arrayOf(mediaType.toString(), "image/gif")
    }
    // =============================================

    private val BUCKET_ORDER_BY = "$COLUMN_DATE_TAKEN DESC"

    /**
     * 获取所有文件夹
     */
    suspend fun getAllFolder(context: Context, type: String, includeGif: Boolean = false) = withContext(Dispatchers.IO) {

        val imageFolderList = mutableListOf<FolderBean>()
        var cursor: Cursor? = null
        try {
            val contentResolver = App.context.contentResolver

            val selectionArgs = if (type == TYPE_IMAGE) {
                SELECTION_ARGS_IMAGE
            } else {
                SELECTION_ARGS_VIDEO
            }
            cursor = contentResolver.query(
                QUERY_URI,
                PROJECTION_29,
                SELECTION_FOR_SINGLE_MEDIA_TYPE_29,
                selectionArgs,
                BUCKET_ORDER_BY
            )

            // Pseudo GROUP BY
            val countMap = HashMap<Long, Long>()
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val bucketId = cursor.getLong(cursor.getColumnIndex(COLUMN_BUCKET_ID))

                    var count = countMap[bucketId]
                    if (count == null) {
                        count = 1L
                    } else {
                        count++
                    }
                    countMap[bucketId] = count
                }
            }

            if (cursor != null) {
                if (cursor.moveToFirst()) {

                    val done = HashSet<Long>()

                    do {
                        val bucketId = cursor.getLong(cursor.getColumnIndex(COLUMN_BUCKET_ID))
                        log("bucketId = $bucketId")
                        if (done.contains(bucketId)) {
                            continue
                        }

                        val fileId = cursor.getLong(
                            cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
                        )
                        log("id = $fileId")
                        val bucketDisplayName = cursor.getString(
                            cursor.getColumnIndex(COLUMN_BUCKET_DISPLAY_NAME)
                        )
                        log("displayName = $bucketDisplayName")
                        val mimeType = cursor.getString(
                            cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
                        )
                        log("mimeType = $mimeType")
                        val uri = getUri(cursor)
                        log("uri = $uri")
                        val count = countMap[bucketId]!!
                        log("count = $count")
                        val date = cursor.getLong(
                            cursor.getColumnIndex(COLUMN_DATE_TAKEN)
                        )
                        log("date = $date")
                        val filePath = cursor.getString(
                            cursor.getColumnIndex(COLUMN_DATA)
                        )
                        val path = filePath.substring(0, filePath.lastIndexOf(File.separatorChar))
                        log("path = $path")

                        //-----------------------------------------
                        val imageFolder = FolderBean()
                        imageFolder.total = count.toInt()
                        val bean = MediaBean()
                        bean.date = date
                        bean.path = filePath

                        bean.uri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            fileId
                        )
                        if (bucketDisplayName == null) {
                            imageFolder.setDirAndName(path)
                        } else {
                            imageFolder.dir = path
                            imageFolder.name = bucketDisplayName
                        }
                        imageFolder.bucketId = bucketId.toString()

                        if (type == TYPE_IMAGE) {
                            when {
                                MediaFile.isGifFileType(bean.path) -> bean.type =
                                    MediaType.TYPE_GIF
                                MediaFile.isJPGFileType(bean.path) -> bean.type =
                                    MediaType.TYPE_JPG
                                MediaFile.isPNGFileType(bean.path) -> bean.type =
                                    MediaType.TYPE_PNG
                                else -> bean.type = MediaType.TYPE_OTHER_IMAGE
                            }

                            if (!includeGif) {
                                val imgExcludeGif =
                                    getImageThumbnailBeanFromPathExcludeGif(
                                        context,
                                        path
                                    )
                                if (imgExcludeGif.size > 0) {
                                    imageFolder.coverMediaBean = (imgExcludeGif[0])
                                    imageFolder.total = imgExcludeGif.size
                                    imageFolderList.add(imageFolder)
                                }
                            } else {
                                imageFolder.coverMediaBean = bean
                                imageFolderList.add(imageFolder)
                            }
                        } else {
                            bean.type = MediaType.TYPE_VIDEO
                            imageFolder.coverMediaBean = bean
                            imageFolderList.add(imageFolder)
                        }

                        done.add(bucketId)

                        log("-----------------------------------------\n\n")
                    } while (cursor.moveToNext())
                }
            }


        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        imageFolderList
    }

    /**
     * 获取某个文件夹下的所有图片的URI
     *
     * @param context
     * @param path
     * @return
     */
    suspend fun getImageThumbnailBeanFromPathExcludeGif(
        context: Context,
        path: String
    ) = withContext(Dispatchers.IO) {
        var result1: MutableList<MediaBean> = mutableListOf()
        var c1: Cursor? = null
        //Images.ImageColumns.DATA 是图片的绝对路径
        try {
            val cr = context.contentResolver
            result1 = ArrayList<MediaBean>()
            c1 = if (TextUtils.isEmpty(path)) {
                cr.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(
                        MediaStore.Images.ImageColumns._ID,
                        MediaStore.Images.ImageColumns.DATA,
                        MediaStore.Images.ImageColumns.DATE_TAKEN,
                        MediaStore.Images.ImageColumns.ORIENTATION
                    ),
                    null,
                    null,
                    MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC" + ", " + MediaStore.Images.ImageColumns._ID + " ASC"
                )
            } else {
                cr.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(
                        MediaStore.Images.ImageColumns._ID,
                        MediaStore.Images.ImageColumns.DATA,
                        MediaStore.Images.ImageColumns.DATE_TAKEN,
                        MediaStore.Images.ImageColumns.ORIENTATION
                    ),
                    MediaStore.Images.ImageColumns.DATA + " like ? ",
                    arrayOf(path + File.separator + "%"),
                    MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC" + ", " + MediaStore.Images.ImageColumns._ID + " ASC"
                )
            }
            if (c1?.moveToFirst() == true) {
                val idIndex = c1.getColumnIndex(MediaStore.Images.ImageColumns._ID)
                val pathIndex = c1.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                val dateIndex = c1.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN)
                val degreeIndex = c1.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION)
                do {
                    val bb = MediaBean()
                    bb.uri = (
                        ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            c1.getInt(idIndex).toLong()
                        )
                    )
                    bb.path = (c1.getString(pathIndex))
                    bb.date = (c1.getLong(dateIndex))
                    bb.degree = (c1.getInt(degreeIndex))
                    if (MediaFile.isGifFileType(bb.path)) {
                        continue
                    } else if (MediaFile.isJPGFileType(bb.path)) {
                        bb.type = (MediaType.TYPE_JPG)
                    } else if (MediaFile.isPNGFileType(bb.path)) {
                        bb.type = (MediaType.TYPE_PNG)
                    } else {
                        bb.type = (MediaType.TYPE_OTHER_IMAGE)
                    }
                    result1.add(bb)
                } while (c1.moveToNext())
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            c1?.close()
        }
        result1
    }

    suspend fun getImageMedia(context: Context, path: String) = withContext(Dispatchers.IO) {
        val cr = context.contentResolver
        val result: MutableList<MediaBean> = mutableListOf()
        var cursor: Cursor? = null
        try {
            cursor = if (TextUtils.isEmpty(path)) {
                cr.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(
                        MediaStore.Images.ImageColumns._ID,
                        MediaStore.Images.ImageColumns.DATA,
                        MediaStore.Images.ImageColumns.DATE_TAKEN,
                        MediaStore.Images.ImageColumns.ORIENTATION
                    ),
                    null,
                    null,
                    MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC" + ", " + MediaStore.Images.ImageColumns._ID + " ASC"
                )
            } else {
                cr.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(
                        MediaStore.Images.ImageColumns._ID,
                        MediaStore.Images.ImageColumns.DATA,
                        MediaStore.Images.ImageColumns.DATE_TAKEN,
                        MediaStore.Images.ImageColumns.ORIENTATION
                    ),
                    MediaStore.Images.ImageColumns.DATA + " like ? ",
                    arrayOf(path + File.separator + "%"),
                    MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC" + ", " + MediaStore.Images.ImageColumns._ID + " ASC"
                )
            }
            if (cursor == null) {
                return@withContext result
            }
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID)
                val pathIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                val dateIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN)
                val degreeIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION)
                do {
                    val bb = MediaBean()
                    bb.uri = (
                        ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            cursor.getInt(idIndex).toLong()
                        )
                    )
                    val thumbPath = cursor.getString(pathIndex)
                    log("Gif: $thumbPath" )

                    if (checkImageError(thumbPath)) {
                        continue
                    }
                    bb.path = (thumbPath)
                    bb.date = (cursor.getLong(dateIndex))
                    bb.degree = (cursor.getInt(degreeIndex))
                    when {
                        MediaFile.isGifFileType(bb.path) -> {
                            bb.type = (MediaType.TYPE_GIF)
                        }
                        MediaFile.isJPGFileType(bb.path) -> {
                            bb.type = (MediaType.TYPE_JPG)
                        }
                        MediaFile.isPNGFileType(bb.path) -> {
                            bb.type = (MediaType.TYPE_PNG)
                        }
                        else -> {
                            bb.type = (MediaType.TYPE_OTHER_IMAGE)
                        }
                    }
                    result.add(bb)
                } while (cursor.moveToNext())
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        result
    }

    suspend fun getVideoMedia(
        context: Context,
        path: String,
        maxDuration: Long
    ) = withContext(Dispatchers.IO) {
        val cr = context.contentResolver
        val result: MutableList<MediaBean> = mutableListOf()
        var cursor: Cursor? = null
        try {
            cursor = if (TextUtils.isEmpty(path)) {
                cr.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(
                        MediaStore.Video.VideoColumns._ID,
                        MediaStore.Video.VideoColumns.DATA,
                        MediaStore.Video.VideoColumns.DATE_TAKEN,
                        MediaStore.Video.VideoColumns.DURATION
                    ),
                    null,
                    null,
                    MediaStore.Video.VideoColumns.DATE_TAKEN + " DESC" + ", " + MediaStore.Video.VideoColumns._ID + " ASC"
                )
            } else {
                cr.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(
                        MediaStore.Video.VideoColumns._ID,
                        MediaStore.Video.VideoColumns.DATA,
                        MediaStore.Video.VideoColumns.DATE_TAKEN,
                        MediaStore.Video.VideoColumns.DURATION
                    ),
                    MediaStore.Video.VideoColumns.DATA + " like ? ",
                    arrayOf(path + File.separator + "%"),
                    MediaStore.Video.VideoColumns.DATE_TAKEN + " DESC" + ", " + MediaStore.Video.VideoColumns._ID + " ASC"
                )
            }
            if (cursor == null) {
                return@withContext result
            }
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID)
                val pathIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA)
                val dateIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_TAKEN)
                val durationIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION)
                do {
                    val bb = MediaBean()
                    bb.uri = (
                        ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            cursor.getInt(idIndex).toLong()
                        )
                    )
                    val videoPath = cursor.getString(pathIndex)
                    if (checkVideoError(videoPath)) {
                        continue
                    }
                    bb.path = (videoPath)
                    bb.date = (cursor.getLong(dateIndex))
                    bb.type = (MediaType.TYPE_VIDEO)

                    //有些文件后缀为视频格式，却不是视频文件，长度为0， 需要排除
                    val time = cursor.getLong(durationIndex)
                    if (time <= 0) {
                        continue
                    }
                    bb.duration = (time)
                    if (maxDuration <= 0 || time <= maxDuration) {
                        result.add(bb)
                    }
                } while (cursor.moveToNext())
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        result
    }

    /***
     *
     * @param path
     * @return error -> true
     */
    private fun checkImageError(path: String): Boolean {
        return if (!isExistFile(path)) {
            true
        } else !MediaFile.isImageFile(path)
    }

    private fun checkVideoError(path: String): Boolean {
        return if (!isExistFile(path)) {
            true
        } else !MediaFile.isVideoFile(path)
    }


    private fun getUri(cursor: Cursor): Uri {
        val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID))
        val mimeType = cursor.getString(
            cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
        )

        val contentUri: Uri = when {
            MimeType.isImage(mimeType) -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            MimeType.isVideo(mimeType) -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else -> // ?
                MediaStore.Files.getContentUri("external")
        }

        return ContentUris.withAppendedId(contentUri, id)
    }



}