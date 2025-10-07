package com.rapid.android.core.domain.model;

public class UserShareBean {

    private CoinBean coinInfo;
    private ArticleListBean shareArticles;

    public UserShareBean() {
        this.coinInfo = new CoinBean();
        this.shareArticles = new ArticleListBean();
    }

    public CoinBean getCoinInfo() {
        return coinInfo;
    }

    public void setCoinInfo(CoinBean coinInfo) {
        this.coinInfo = coinInfo;
    }

    public ArticleListBean getShareArticles() {
        return shareArticles;
    }

    public void setShareArticles(ArticleListBean shareArticles) {
        this.shareArticles = shareArticles;
    }
}
