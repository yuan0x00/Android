package com.rapid.android.core.domain.model;

public class UserToolBean {

    private String desc;
    private String icon;
    private int id;
    private String link;
    private String name;
    private int order;
    private int type;
    private int userId;
    private int visible;

    public UserToolBean() {
        this.desc = "";
        this.icon = "";
        this.id = 0;
        this.link = "";
        this.name = "";
        this.order = 0;
        this.type = 0;
        this.userId = 0;
        this.visible = 0;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getVisible() {
        return visible;
    }

    public void setVisible(int visible) {
        this.visible = visible;
    }
}
