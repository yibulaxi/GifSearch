package com.funny.app.gif.memes.ui.mvp.presenter

import android.Manifest
import com.funny.app.gif.memes.R
import com.funny.app.gif.memes.app.GlobalObj
import com.funny.app.gif.memes.bean.BackupBean
import com.funny.app.gif.memes.bean.event.RestoreLikeGifEvent
import com.funny.app.gif.memes.ui.mvp.view.BackupRestoreView
import com.funny.app.gif.memes.util.DataBaseHelper
import com.funny.app.gif.memes.util.JsonUtils

import com.funny.lib.common.mvp.BasePresenter
import com.funny.lib.common.util.*
import com.funny.lib.permission.PermissionListener
import com.funny.lib.permission.PermissionManager
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus

class BackupRestorePresenter : BasePresenter<BackupRestoreView>() {
    private val BACKUP_FILE_PATH = GlobalObj.backupFilePath


    fun backup(task: Runnable) {

        PermissionManager.request(object : PermissionListener {
            override fun onGranted(grantedList: MutableList<String>) {
                kotlin.run {
                    val likeList = DataBaseHelper.getAllLikeItem()
                    if (likeList.isEmpty()) {
                        toast(R.string.no_backup_data)
                        task.run()
                        return
                    }
                    val backupBean = BackupBean()
                    backupBean.data = likeList
                    val result = Gson().toJson(backupBean)
                    log("backupResult = $result")
                    val success = FileUtil.saveStringToFile(result, BACKUP_FILE_PATH)
                    if (success) {
                        toast(R.string.backup_success)
                    } else {
                        toast(R.string.backup_fail)
                    }
                    task.run()
                }
            }

            override fun onDenied(deniedList: MutableList<String>) {
                toast(R.string.no_wire_store_permission_tips)
                task.run()
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    }

    fun restore(task: Runnable) {
        PermissionManager.request(object : PermissionListener {
            override fun onGranted(grantedList: MutableList<String>) {
                kotlin.run {
                    val data = FileUtil.readFileToString(BACKUP_FILE_PATH)
                    if (data == null || data.isEmpty()) {
                        toast(R.string.no_backup_data)
                        task.run()
                        return
                    }

                    try {
                        val backupBean = JsonUtils.json2Object(data, BackupBean::class.java)
                        val likeList = backupBean?.data
                        likeList?.map {
                            val likeItem = DataBaseHelper.getLikeItem(it.gifId)
                            if (likeItem == null) {
                                DataBaseHelper.liked(it.gifId, it.data)
                            }
                        }
                        EventBus.getDefault().post(RestoreLikeGifEvent())
                        toast(R.string.restore_success)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        toast(R.string.restore_fail)
                    }
                    task.run()
                }
            }

            override fun onDenied(deniedList: MutableList<String>) {
                toast(R.string.no_read_store_permission_tips)
                task.run()
            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    fun delBackup(task: Runnable) {

        PermissionManager.request(object : PermissionListener {
            override fun onGranted(grantedList: MutableList<String>) {
                kotlin.run {
                    FileUtil.deleteFile(BACKUP_FILE_PATH)
                    task.run()
                    toast(getString(R.string.backup_success))
                }
            }

            override fun onDenied(deniedList: MutableList<String>) {
                toast(R.string.no_wire_store_permission_tips)
                task.run()
            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE)

    }
}