package com.rapid.android.core.domain.model;

import java.util.ArrayList;
import java.util.List;

public class NavigationBean {

    private int cid;
    private String name;
    private List<ArticleListBean.Data> articles;

    public NavigationBean() {
        this.name = "";
        this.articles = new ArrayList<>();
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ArticleListBean.Data> getArticles() {
        return articles;
    }

    public void setArticles(List<ArticleListBean.Data> articles) {
        this.articles = articles;
    }
}
