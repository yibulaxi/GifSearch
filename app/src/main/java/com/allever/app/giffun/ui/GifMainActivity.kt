package com.allever.app.giffun.ui

import android.Manifest
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.allever.app.giffun.R
import com.allever.app.giffun.app.Global
import com.allever.app.giffun.bean.DataBean
import com.allever.app.giffun.bean.TrendingResponse
import com.allever.app.giffun.function.download.DownloadManager
import com.allever.app.giffun.ui.adapter.GifAdapter
import com.allever.app.giffun.ui.mvp.model.RetrofitUtil
import com.allever.app.giffun.util.SpUtils
import com.allever.lib.common.app.BaseActivity
import com.allever.lib.common.util.ToastUtils
import com.allever.lib.common.util.log
import com.allever.lib.permission.PermissionListener
import com.allever.lib.permission.PermissionManager
import kotlinx.android.synthetic.main.activity_gif_main.*
import rx.Subscriber

class GifMainActivity: BaseActivity() {

    private var mAdapter: GifAdapter? = null
    private var mGifDataList = mutableListOf<DataBean>()
    private lateinit var mProgressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gif_main)

        mProgressDialog = ProgressDialog(this)

        mAdapter = GifAdapter(this, R.layout.item_gif, mGifDataList)

        gifRecyclerView.layoutManager = LinearLayoutManager(this)
        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(gifRecyclerView)
        gifRecyclerView.adapter = mAdapter

        PermissionManager.request(object : PermissionListener {
            override fun onGranted(grantedList: MutableList<String>) {
                Global.createDir()
                getData()
            }

            override fun onDenied(deniedList: MutableList<String>) {
                ToastUtils.show("拒绝权限无法使用")
                finish()
            }

            override fun alwaysDenied(deniedList: MutableList<String>) {
                PermissionManager.jumpPermissionSetting(this@GifMainActivity,0,
                    DialogInterface.OnClickListener { dialog, which ->
                        ToastUtils.show("手动授权")
                        finish()
                    })
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun onDestroy() {
        super.onDestroy()
        DownloadManager.getInstance().cancelAllTask()
    }

    private fun getData() {
        val count = 10
        var offset = SpUtils.getString(Global.SP_OFFSET, "0")
        log("offset = $offset")
        showLoadingProgressDialog("正在加载...")
        RetrofitUtil.trendingGif(offset.toInt(), count, object : Subscriber<TrendingResponse>() {
            override fun onCompleted() {}
            override fun onError(e: Throwable) {
                e.printStackTrace()
                log("请求失败")
                hideLoadingProgressDialog()
            }

            override fun onNext(bean: TrendingResponse) {
                hideLoadingProgressDialog()
                log("请求成功")
                val data = bean.data
                data?.map {
                    log("trending = ${it.images.original.url}")
                }

                mGifDataList.clear()
                mGifDataList.addAll(data)
                mAdapter?.notifyDataSetChanged()

                offset = if (mGifDataList.size < count) {
                    "0"
                } else {
                    (offset.toInt() + count).toString()
                }
                SpUtils.putString(Global.SP_OFFSET, offset)
            }
        })
    }

    private fun showLoadingProgressDialog(msg: String) {
        if (!mProgressDialog.isShowing) {
            mProgressDialog.setMessage(msg)
            mProgressDialog.show()
        }
    }

    private fun hideLoadingProgressDialog() {
        if (mProgressDialog.isShowing) {
            mProgressDialog.dismiss()
        }
    }
}