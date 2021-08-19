package com.allever.app.gif.search.ui.maker.adapter.model

import androidx.databinding.ObservableField
import com.xm.lib.base.adapter.recyclerview.BaseRvCustomViewModelKt

class MediaItemViewModel: BaseRvCustomViewModelKt() {
    val selected = ObservableField<Boolean>()
}