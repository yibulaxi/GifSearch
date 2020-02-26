package com.allever.app.gif.search.bean;

import org.litepal.crud.LitePalSupport;

public class LikedItem extends LitePalSupport {
    private String gifId;
    private String data;

    public String getGifId() {
        return gifId;
    }

    public void setGifId(String gifId) {
        this.gifId = gifId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
