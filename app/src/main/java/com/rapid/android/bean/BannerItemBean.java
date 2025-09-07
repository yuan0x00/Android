package com.rapid.android.bean;

import androidx.annotation.Keep;

@Keep
public class BannerItemBean {
    private String desc;
    private int id;
    private String imagePath;
    private int isVisible;
    private int order;
    private String title;
    private int type;
    private String url;

    public BannerItemBean(String imagePath) {
        this.imagePath = imagePath;
    }

    public BannerItemBean() {
        this.desc = "";
        this.id = 0;
        this.imagePath = "";
        this.isVisible = 0;
        this.order = 0;
        this.title = "";
        this.type = 0;
        this.url = "";
    }

    public BannerItemBean(String desc, int id, String imagePath, int isVisible,
                          int order, String title, int type, String url) {
        this.desc = desc != null ? desc : "";
        this.id = id;
        this.imagePath = imagePath != null ? imagePath : "";
        this.isVisible = isVisible;
        this.order = order;
        this.title = title != null ? title : "";
        this.type = type;
        this.url = url != null ? url : "";
    }

    // Getters
    public String getDesc() {
        return desc;
    }

    // Setters
    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(int isVisible) {
        this.isVisible = isVisible;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}