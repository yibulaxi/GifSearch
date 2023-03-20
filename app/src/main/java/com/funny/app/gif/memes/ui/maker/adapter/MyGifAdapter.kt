package com.funny.app.gif.memes.ui.maker.adapter

import android.content.Context
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import com.funny.app.gif.memes.R
import com.funny.app.gif.memes.ui.adapter.bean.GifItemBean
import com.funny.lib.common.ui.widget.recycler.BaseRecyclerViewAdapter
import com.funny.lib.common.ui.widget.recycler.BaseViewHolder
import com.funny.lib.common.util.log
import com.bumptech.glide.Glide

class MyGifAdapter(context: Context, resId: Int, data: MutableList<GifItemBean>) :
    BaseRecyclerViewAdapter<GifItemBean>(context, resId, data) {

    var itemOptionListener: OnItemOptionClick? = null
    var editMode = false
        set(value) {
            field = value
            if (!editMode) {
                allMode = false
            }
            notifyDataSetChanged()
        }
    var allMode = false
    var allCheck = false
        set(value) {
            field = value
            mData.map {
                it.checked = value
            }
            notifyDataSetChanged()
            if (value) {
                selectedItem.addAll(mData)
            } else {
                selectedItem.clear()
            }
        }
    var selectedItem = mutableSetOf<GifItemBean>()


    override fun bindHolder(holder: BaseViewHolder, position: Int, item: GifItemBean) {
        val url = item.url
        log("LikedAdapter: url = ${item.url}")
        val ivGif = holder.getView<ImageView>(R.id.ivGif)
        Glide.with(mContext).asBitmap().load(url).into(ivGif!!)

        val ivMaks = holder.getView<View>(R.id.ivMask)

        val cb = holder.getView<CheckBox>(R.id.cbSelect)
        cb?.visibility = if (editMode) View.VISIBLE else View.GONE
        cb?.setOnCheckedChangeListener { buttonView, isChecked ->
            ivMaks?.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        if (editMode) {
            ivMaks?.visibility = if (cb?.isChecked == true) View.VISIBLE else View.GONE
        } else {
            ivMaks?.visibility = View.GONE
        }

        if (allMode) {
            holder.setChecked(R.id.cbSelect, allCheck)
        } else {
            holder.setChecked(R.id.cbSelect, item.checked)
        }

        holder.itemView.setOnClickListener {
            if (editMode) {
                handleCheck(holder, item, cb)
            } else {
                itemOptionListener?.onItemClicked(position)
            }
        }

        holder.itemView.setOnLongClickListener {
            if (editMode) {
                return@setOnLongClickListener true
            } else {
                itemOptionListener?.onLongClick(position)
                return@setOnLongClickListener true
            }
        }
    }

    private fun handleCheck(holder: BaseViewHolder, item: GifItemBean, cb: CheckBox?) {
        allMode = false
        item.checked = !item.checked
        holder.setChecked(R.id.cbSelect, item.checked)
        if (cb?.isChecked == true) {
            selectedItem.add(item)
        } else {
            selectedItem.remove(item)
        }
    }

    public interface OnItemOptionClick {
        fun onItemClicked(position: Int)
        fun onLongClick(position: Int)
    }
}