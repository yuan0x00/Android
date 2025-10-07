package com.core.domain.model;

public class ToolItemBean {

    private String desc;
    private String icon;
    private int id;
    private int isNew;
    private String link;
    private String name;
    private int order;
    private int showInTab;
    private String tabName;
    private int visible;

    public ToolItemBean() {
        this.desc = "";
        this.icon = "";
        this.id = 0;
        this.isNew = 0;
        this.link = "";
        this.name = "";
        this.order = 0;
        this.showInTab = 0;
        this.tabName = "";
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

    public int getIsNew() {
        return isNew;
    }

    public void setIsNew(int isNew) {
        this.isNew = isNew;
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

    public int getShowInTab() {
        return showInTab;
    }

    public void setShowInTab(int showInTab) {
        this.showInTab = showInTab;
    }

    public String getTabName() {
        return tabName;
    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }

    public int getVisible() {
        return visible;
    }

    public void setVisible(int visible) {
        this.visible = visible;
    }
}
