package com.rapid.android.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @NonNull
    @PrimaryKey
    private String id;
    
    private String username;
    private String email;
    private String token;
    private String icon;
    private int coinCount;
    private boolean isAdmin;
    private String nickname;

    // No-argument constructor required by Room
    public UserEntity() {
    }

    // Static factory method for creating instances with values
    public static UserEntity create(@NonNull String id, String username, String email, String token, String icon,
                                   int coinCount, boolean isAdmin, String nickname) {
        UserEntity user = new UserEntity();
        user.id = id;
        user.username = username;
        user.email = email;
        user.token = token;
        user.icon = icon;
        user.coinCount = coinCount;
        user.isAdmin = isAdmin;
        user.nickname = nickname;
        return user;
    }

    // Getter 和 Setter 方法
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getCoinCount() {
        return coinCount;
    }

    public void setCoinCount(int coinCount) {
        this.coinCount = coinCount;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}