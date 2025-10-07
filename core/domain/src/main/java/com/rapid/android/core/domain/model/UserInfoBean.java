package com.rapid.android.core.domain.model;

public class UserInfoBean {
    private LoginBean userInfo;
    private CoinBean coinInfo;
    private CollectArticleInfoBean collectArticleInfo;

    public UserInfoBean() {
        this.userInfo = new LoginBean();
        this.coinInfo = new CoinBean();
        this.collectArticleInfo = new CollectArticleInfoBean();
    }

    public UserInfoBean(LoginBean userInfo, CoinBean coinInfo) {
        this.userInfo = userInfo != null ? userInfo : new LoginBean();
        this.coinInfo = coinInfo != null ? coinInfo : new CoinBean();
        this.collectArticleInfo = new CollectArticleInfoBean();
    }

    public UserInfoBean(LoginBean userInfo, CoinBean coinInfo, CollectArticleInfoBean collectArticleInfo) {
        this.userInfo = userInfo != null ? userInfo : new LoginBean();
        this.coinInfo = coinInfo != null ? coinInfo : new CoinBean();
        this.collectArticleInfo = collectArticleInfo != null ? collectArticleInfo : new CollectArticleInfoBean();
    }

    // Getters
    public LoginBean getUserInfo() {
        return userInfo;
    }

    // Setters
    public void setUserInfo(LoginBean userInfo) {
        this.userInfo = userInfo;
    }

    public CoinBean getCoinInfo() {
        return coinInfo;
    }

    public void setCoinInfo(CoinBean coinInfo) {
        this.coinInfo = coinInfo;
    }

    public CollectArticleInfoBean getCollectArticleInfo() {
        return collectArticleInfo;
    }

    public void setCollectArticleInfo(CollectArticleInfoBean collectArticleInfo) {
        this.collectArticleInfo = collectArticleInfo;
    }
}
