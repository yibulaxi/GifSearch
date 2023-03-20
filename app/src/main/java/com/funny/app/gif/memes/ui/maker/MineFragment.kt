package com.funny.app.gif.memes.ui.maker

import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import com.funny.app.gif.memes.BR
import com.funny.app.gif.memes.R
import com.funny.app.gif.memes.app.BaseFragment2
import com.funny.app.gif.memes.databinding.FragmentMineBinding
import com.funny.app.gif.memes.event.GifMakeEvent
import com.funny.app.gif.memes.function.store.Repository
import com.funny.app.gif.memes.ui.GifPreviewActivity
import com.funny.app.gif.memes.ui.adapter.bean.GifItemBean
import com.funny.app.gif.memes.ui.maker.adapter.MyGifAdapter
import com.funny.app.gif.memes.ui.maker.model.MineViewModel
import com.funny.lib.common.util.SystemUtils
import com.funny.lib.common.util.log
import com.funny.lib.common.util.toast
import com.google.gson.Gson
import com.xm.lib.base.config.DataBindingConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

class MineFragment: BaseFragment2<FragmentMineBinding, MineViewModel>(), View.OnClickListener {


    private lateinit var mAdapter: MyGifAdapter

    private var mEditMode = false
        set(value) {
            field = value
            mBinding.rlBottomToolBar.visibility = if (value) View.VISIBLE else View.GONE
            mAdapter.editMode = value
        }

    private var mData = mutableListOf<GifItemBean>()
    private var mCurrentItem: GifItemBean? = null

    override fun initDataBindingConfig() = DataBindingConfig(R.layout.fragment_mine, BR.mineViewModel)

    override fun initDataAndEvent() {
        EventBus.getDefault().register(this)
        mBinding.cbBottomBarCheckAll.setOnCheckedChangeListener { buttonView, isChecked ->
            mAdapter.allMode = true
            mAdapter.allCheck = isChecked
        }
        mBinding.ivBottomBarBack.setOnClickListener(this)
        mBinding.ivBottomBarDelete.setOnClickListener(this)

        mAdapter = MyGifAdapter(context!!, R.layout.item_liked, mData)
        mBinding.rvLiked.layoutManager = GridLayoutManager(context, 3)
        mBinding.rvLiked.adapter = mAdapter
        val gson = Gson()
        mAdapter.itemOptionListener = object : MyGifAdapter.OnItemOptionClick {
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

    private fun getLikedData() {
        GlobalScope.launch(Dispatchers.Main) {
            val result = Repository.getMyGif()
            result.map {
                log("Gif: ${it.url}")
            }

            initEditMode()
            mData.clear()
            mData.addAll(result)
            mAdapter.notifyDataSetChanged()
        }
    }

    private fun initEditMode() {
        mAdapter.selectedItem.clear()
        mEditMode = false
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
                        mViewModel.viewModelScope.launch {
                            mAdapter.selectedItem.map {
                                com.android.absbase.utils.FileUtils.delete(File(it.url), true)
                            }
                            getLikedData()
                        }
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLikeUpdate(gifMakeEvent: GifMakeEvent) {
        getLikedData()
    }
}