
package com.allever.app.gif.search.ui.maker

import android.graphics.Rect
import android.view.View
import com.allever.app.gif.search.BR
import com.allever.app.gif.search.R
import com.allever.app.gif.search.app.BaseDataActivity2
import com.allever.app.gif.search.databinding.ActivityPickBinding
import com.allever.app.gif.search.ui.maker.model.PickViewModel
import com.android.absbase.utils.ResourcesUtils
import com.xm.lib.base.config.DataBindingConfig

class PickActivity : BaseDataActivity2<ActivityPickBinding, PickViewModel>(){

    override fun initDataBindingConfig() = DataBindingConfig(R.layout.activity_pick, BR.pickViewModel)

    override fun initDataAndEvent() {
        mBinding.includeTopBar.tvLabel.text = getStringRes(R.string.choose_video)
        mBinding.includeTopBar.ivLeft.setOnClickListener {
            finish()
        }


        val spacingInPixels = ResourcesUtils.getDimension(R.dimen.item_cell_space_width).toInt()
        val firstTopSpacing = ResourcesUtils.getDimension(R.dimen.item_cell_space_top).toInt()
        val bottomSpacing = ResourcesUtils.getDimension(R.dimen.item_cell_space_width).toInt()
        val middleSpacing = ResourcesUtils.getDimension(R.dimen.item_cell_space_middle).toInt()
        mBinding.rvMedia.addItemDecoration(object : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View,
                parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State
            ) {
                val COL = 3
                val pos = parent.getChildLayoutPosition(view)
                if (pos / COL == 0) {
                    //设置第一行
                    outRect.top = firstTopSpacing
                }

                outRect.bottom = bottomSpacing
            }
        })
    }

    override fun destroyView() {
    }

}