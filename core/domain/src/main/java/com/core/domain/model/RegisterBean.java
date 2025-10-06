package com.core.domain.model;

import java.util.ArrayList;
import java.util.List;

public class RegisterBean {
    private boolean admin;
    private List<String> chapterTops;
    private int coinCount;
    private List<String> collectIds;
    private String email;
    private String icon;
    private int id;
    private String nickname;
    private String password;
    private String publicName;
    private String token;
    private int type;
    private String username;

    public RegisterBean() {
        this.admin = false;
        this.chapterTops = new ArrayList<>();
        this.coinCount = 0;
        this.collectIds = new ArrayList<>();
        this.email = "";
        this.icon = "";
        this.id = 0;
        this.nickname = "";
        this.password = "";
        this.publicName = "";
        this.token = "";
        this.type = 0;
        this.username = "";
    }

    public RegisterBean(boolean admin, List<String> chapterTops, int coinCount,
                        List<String> collectIds, String email, String icon, int id,
                        String nickname, String password, String publicName,
                        String token, int type, String username) {
        this.admin = admin;
        this.chapterTops = chapterTops != null ? chapterTops : new ArrayList<>();
        this.coinCount = coinCount;
        this.collectIds = collectIds != null ? collectIds : new ArrayList<>();
        this.email = email != null ? email : "";
        this.icon = icon != null ? icon : "";
        this.id = id;
        this.nickname = nickname != null ? nickname : "";
        this.password = password != null ? password : "";
        this.publicName = publicName != null ? publicName : "";
        this.token = token != null ? token : "";
        this.type = type;
        this.username = username != null ? username : "";
    }

    // Getters
    public boolean isAdmin() {
        return admin;
    }

    // Setters
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public List<String> getChapterTops() {
        return chapterTops;
    }

    public void setChapterTops(List<String> chapterTops) {
        this.chapterTops = chapterTops;
    }

    public int getCoinCount() {
        return coinCount;
    }

    public void setCoinCount(int coinCount) {
        this.coinCount = coinCount;
    }

    public List<String> getCollectIds() {
        return collectIds;
    }

    public void setCollectIds(List<String> collectIds) {
        this.collectIds = collectIds;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPublicName() {
        return publicName;
    }

    public void setPublicName(String publicName) {
        this.publicName = publicName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}