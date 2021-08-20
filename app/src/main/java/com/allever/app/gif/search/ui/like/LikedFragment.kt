package com.allever.app.gif.search.ui.like

import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.allever.app.gif.search.BR
import com.allever.app.gif.search.R
import com.allever.app.gif.search.app.BaseFragment2
import com.allever.app.gif.search.app.Global
import com.allever.app.gif.search.bean.event.DownloadFinishEvent
import com.allever.app.gif.search.bean.event.LikeEvent
import com.allever.app.gif.search.bean.event.RemoveLikeListEvent
import com.allever.app.gif.search.bean.event.RestoreLikeEvent
import com.allever.app.gif.search.databinding.FragmentLikedBinding
import com.allever.app.gif.search.ui.GifPreviewActivity
import com.allever.app.gif.search.ui.adapter.bean.GifItem
import com.allever.app.gif.search.ui.like.adapter.GifLikedAdapter
import com.allever.app.gif.search.ui.like.model.LikedViewModel
import com.allever.app.gif.search.util.DBHelper
import com.allever.lib.common.util.SystemUtils
import com.allever.lib.common.util.toast
import com.google.gson.Gson
import com.xm.lib.base.config.DataBindingConfig
import com.xm.lib.base.ui.BaseFragmentKt
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class LikedFragment : BaseFragment2<FragmentLikedBinding, LikedViewModel>(), View.OnClickListener {

    private lateinit var mAdapter: GifLikedAdapter

    private var mEditMode = false
        set(value) {
            field = value
            mBinding.rlBottomToolBar.visibility = if (value) View.VISIBLE else View.GONE
            mAdapter.editMode = value
        }

    private var mData = mutableListOf<GifItem>()
    private var mCurrentItem: GifItem? = null

    override fun initDataBindingConfig() = DataBindingConfig(R.layout.fragment_liked, BR.likedViewModel)

    override fun initDataAndEvent() {
        EventBus.getDefault().register(this)

        mBinding.cbBottomBarCheckAll.setOnCheckedChangeListener { buttonView, isChecked ->
            mAdapter.allMode = true
            mAdapter.allCheck = isChecked
        }
        mBinding.ivBottomBarBack.setOnClickListener(this)
        mBinding.ivBottomBarDelete.setOnClickListener(this)

        mAdapter = GifLikedAdapter(context!!, R.layout.item_liked, mData)
        mBinding.rvLiked.layoutManager = GridLayoutManager(context, 3)
        mBinding.rvLiked.adapter = mAdapter
        val gson = Gson()
        mAdapter.itemOptionListener = object : GifLikedAdapter.OnItemOptionClick {
            override fun onItemClicked(position: Int) {
                mCurrentItem = mData[position]
                val item = mData[position]
                GifPreviewActivity.start(context!!, gson.toJson(item))
            }

            override fun onLongClick(position: Int) {
                if (!mEditMode) {
                    mEditMode = true
                }
            }
        }

        val layoutParams = mBinding.rvLiked.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = layoutParams.topMargin + SystemUtils.getStatusBarHeight(context!!)
        mBinding.rvLiked.layoutParams = layoutParams

        getLikedData()
    }

    override fun destroyView() {
        EventBus.getDefault().unregister(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBottomBarBack -> {
                mEditMode = false
            }
            R.id.ivBottomBarDelete -> {
                if (mAdapter.selectedItem.isEmpty()) {
                    toast(R.string.un_slelectd)
                    return
                }
                AlertDialog.Builder(activity!!)
                    .setMessage(R.string.remove_tips)
                    .setCancelable(true)
                    .setPositiveButton(R.string.ok) { dialog, which ->
                        //                        mPresenter.removeLikes(mAdapter.selectedItem)
                        val idList = mutableListOf<String>()
                        mAdapter.selectedItem.map {
                            DBHelper.unLiked(it.id.toString())
                            idList.add(it.id.toString())
                        }
                        dialog.dismiss()
                        getLikedData()
                        val removeLikeListEvent = RemoveLikeListEvent()
                        removeLikeListEvent.gifIdList = idList
                        EventBus.getDefault().post(removeLikeListEvent)
                    }
                    .setNegativeButton(R.string.cancle) { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (mEditMode) {
            mEditMode = false
            return true
        }
        return false
    }

    private fun initEditMode() {
        mAdapter.selectedItem.clear()
        mEditMode = false
    }
    private fun getLikedData() {
        initEditMode()
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLikeRestoreEvent(event: RestoreLikeEvent) {
        getLikedData()
    }
}