package com.allever.app.gif.search.ui.maker.adapter.view

import android.content.Context
import android.util.AttributeSet
import com.allever.app.gif.search.BR
import com.allever.app.gif.search.R
import com.allever.app.gif.search.databinding.ItemMediaBinding
import com.allever.app.gif.search.ui.maker.adapter.bean.MediaItem
import com.allever.app.gif.search.ui.maker.adapter.model.MediaItemViewModel
import com.bumptech.glide.Glide
import com.xm.lib.base.adapter.recyclerview.BaseRecyclerAdapter
import com.xm.lib.base.adapter.recyclerview.BaseRvCustomViewKt
import com.xm.lib.base.adapter.recyclerview.BaseViewHolder
import com.xm.lib.base.config.DataBindingConfig

class MediaItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) :  BaseRvCustomViewKt<MediaItem, MediaItemViewModel>(context, attrs, defStyleAttr) {
    override fun convert(
        adapter: BaseRecyclerAdapter<*, out BaseViewHolder<*>>?,
        data: MediaItem?,
        position: Int
    ) {
        val binding = getDataBinding<ItemMediaBinding>()
        Glide.with(mCxt).load(data?.data?.path).into(binding.ivImage)
        mViewModel.selected.set(data?.selected)
        binding.itemView.setOnClickListener {
            adapter?.callItemClicked(it, position)
        }

        binding.itemView.setOnLongClickListener {
            return@setOnLongClickListener adapter?.callItemLongClicked(it, position)?: false
        }
    }

    override fun getViewModel(): MediaItemViewModel = MediaItemViewModel()

    override fun getDataBindingConfig() = DataBindingConfig(R.layout.item_media, BR.mediaItemViewModel)

}