package com.lib.domain.model;

import androidx.annotation.Keep;

@Keep
public class CoinBean {
    private int coinCount;
    private int level;
    private String nickname;
    private String rank;
    private int userId;
    private String username;

    public CoinBean() {
        this.coinCount = 0;
        this.level = 0;
        this.nickname = "";
        this.rank = "";
        this.userId = 0;
        this.username = "";
    }

    public CoinBean(int coinCount, int level, String nickname, String rank, int userId, String username) {
        this.coinCount = coinCount;
        this.level = level;
        this.nickname = nickname != null ? nickname : "";
        this.rank = rank != null ? rank : "";
        this.userId = userId;
        this.username = username != null ? username : "";
    }

    // Getters
    public int getCoinCount() {
        return coinCount;
    }

    // Setters
    public void setCoinCount(int coinCount) {
        this.coinCount = coinCount;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}