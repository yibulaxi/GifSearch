package com.allever.app.giffun.ui

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.allever.app.giffun.R
import com.allever.app.giffun.app.BaseFragment
import com.allever.app.giffun.app.Global
import com.allever.app.giffun.bean.DataBean
import com.allever.app.giffun.bean.event.LikeEvent
import com.allever.app.giffun.bean.event.DownloadFinishEvent
import com.allever.app.giffun.ui.adapter.GifLikedAdapter
import com.allever.app.giffun.ui.mvp.presenter.LikedPresenter
import com.allever.app.giffun.ui.mvp.view.LikedView
import com.allever.app.giffun.util.DBHelper
import com.allever.lib.common.ui.widget.recycler.BaseViewHolder
import com.allever.lib.common.ui.widget.recycler.ItemListener
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class LikedFragment: BaseFragment<LikedView, LikedPresenter>(), LikedView {

    private lateinit var mRv: RecyclerView
    private lateinit var mAdapter: GifLikedAdapter

    private var mData = mutableListOf<DataBean>()
    private var mCurrentItem: DataBean? = null

    override fun getContentView(): Int = R.layout.fragment_liked

    override fun initView(root: View) {

        EventBus.getDefault().register(this)

        mAdapter = GifLikedAdapter(context!!, R.layout.item_liked, mData)
        mRv = root.findViewById(R.id.rvLiked)
        mRv.layoutManager = GridLayoutManager(context, 3)
        mRv.adapter = mAdapter

        val gson = Gson()
        mAdapter.setItemListener(object: ItemListener {
            override fun onItemClick(position: Int, holder: BaseViewHolder) {
                mCurrentItem = mData[position]
                val item = mData[position]
                GifPreviewActivity.start(context!!, gson.toJson(item))
            }
        })
    }

    override fun initData() {
        getLikedData()
    }

    override fun createPresenter(): LikedPresenter = LikedPresenter()

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    private fun getLikedData() {
        val datas = DBHelper.getLikedList()
        mData.clear()
        mData.addAll(datas)
        mAdapter.notifyDataSetChanged()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLikeUpdate(likeEvent: LikeEvent) {
        val dataBean = likeEvent.dataBean
        if (likeEvent.isLiked) {
            //在后面追加
            if (dataBean != null) {
                mData.add(likeEvent.dataBean!!)
            }
        } else {
            //移除index
            val position = Global.getIndex(likeEvent.id, mData)
            mData.removeAt(position)
        }

        mAdapter.notifyDataSetChanged()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownloadFinishEvent(event: DownloadFinishEvent) {
        getLikedData()
    }
}