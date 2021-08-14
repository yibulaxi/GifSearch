package com.allever.app.gif.search.ad

import com.allever.lib.ad.chain.AdChainListener
import com.allever.lib.ad.chain.IAd

interface SimpleAdChainListener: AdChainListener {
    override fun onLoaded(ad: IAd?) {

    }

    override fun onFailed(msg: String) {
    }

    override fun onShowed() {
    }

    override fun onDismiss() {
    }
}