package com.allever.app.gif.memes.ui.maker.model

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.allever.app.gif.memes.R
import com.allever.app.gif.memes.event.GifMakeEvent
import com.allever.app.gif.memes.function.maker.GifMakeHelper
import com.allever.app.gif.memes.function.media.MediaBean
import com.allever.lib.common.util.FileUtils
import com.allever.lib.common.util.getString
import com.allever.lib.common.util.log
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModelKt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.io.File

class GifMakerViewModel: BaseViewModelKt<IBaseView>() {
    var mediaBean: MediaBean? = null
    var startPosition = 0
    var endPosition = 0
    val startText = MutableLiveData<String>()
    val endText = MutableLiveData<String>()
    val durationText = MutableLiveData<String>()
    val confirmClickAble = MutableLiveData<Boolean>()
    val confirmText = MutableLiveData<String>()
    override fun onCreated() {
        confirmClickAble.value = true
        confirmText.value = getString(R.string.ok)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun onClickConfirm() {
        mediaBean?.let {
            viewModelScope.launch(Dispatchers.Main) {
                val fileName = it.name
                val gifName = if (fileName.contains(".")) {
                    "${fileName.split(".")[0]}_${startPosition}_${endPosition}.gif"
                } else {
                    "${fileName}_${startPosition}_${endPosition}.gif"
                }
                val toFile = "${GifMakeHelper.gifDir}${File.separator}${gifName}"
                if (FileUtils.checkExist(toFile)) {
                    log("${toFile}已存在")
                    return@launch
                }
                confirmClickAble.value = false
                confirmText.value = "正在转换"
                val result = GifMakeHelper.makeGif(mCxt, it.path, toFile, startPosition,  endPosition, 200) { current, total ->
                    val logMsg = "$current/$total"
                    log(logMsg)
                    confirmText.postValue(logMsg)
                }
                log("完成：$result -> $toFile" )
                if (result) {
                    EventBus.getDefault().post(GifMakeEvent())
                    delay(1000)
                    finish()
                } else  {
                    com.android.absbase.utils.FileUtils.delete(File(toFile), true)
                }
                confirmClickAble.value = true
            }
        }

    }
}