package com.lib.domain.model;

public class CollectArticleInfoBean {

    private int count;

    public CollectArticleInfoBean() {
        this.count = 0;
    }

    public CollectArticleInfoBean(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
