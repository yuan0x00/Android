package com.rapid.android.domain.model;

import androidx.annotation.Keep;

@Keep
public class UserInfoBean {
    private LoginBean userInfo;
    private CoinBean coinInfo;

    public UserInfoBean() {
        this.userInfo = new LoginBean();
        this.coinInfo = new CoinBean();
    }

    public UserInfoBean(LoginBean userInfo, CoinBean coinInfo) {
        this.userInfo = userInfo != null ? userInfo : new LoginBean();
        this.coinInfo = coinInfo != null ? coinInfo : new CoinBean();
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
}