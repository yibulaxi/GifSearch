package com.funny.lib.common.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.funny.lib.common.R
import com.funny.lib.common.app.App

object FeedbackHelper {

    private val FEEDBACK_TO = "funntappptech@hotmail.com"
    private val SUBJECT = ""

    fun feedback(activity: Context?) {
        // 必须明确使用mailto前缀来修饰邮件地址,如果使用
        // intent.putExtra(Intent.EXTRA_EMAIL, email)，结果将匹配不到任何应用
        val uri = Uri.parse("mailto:" + FEEDBACK_TO)
        val email = arrayOf(FEEDBACK_TO)
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.putExtra(Intent.EXTRA_CC, email)
        intent.putExtra(Intent.EXTRA_SUBJECT, App.context.getString(R.string.common_feed_back_subject))
        intent.putExtra(Intent.EXTRA_TEXT, App.context.getString(R.string.common_feed_back_content))
        activity?.startActivity(Intent.createChooser(intent, App.context.getString(R.string.common_select_mail_app)))
    }
}