package com.lib.domain.model;

import java.util.ArrayList;
import java.util.List;

public class CategoryNodeBean {

    private int courseId;
    private int id;
    private String name;
    private int order;
    private int parentChapterId;
    private boolean userControlSetTop;
    private int visible;
    private int type;
    private String author;
    private String cover;
    private String desc;
    private String lisense;
    private String lisenseLink;
    private String link;
    private List<ArticleListBean.Data> articleList;
    private List<CategoryNodeBean> children;

    public CategoryNodeBean() {
        this.children = new ArrayList<>();
        this.articleList = new ArrayList<>();
        this.name = "";
        this.author = "";
        this.cover = "";
        this.desc = "";
        this.lisense = "";
        this.lisenseLink = "";
        this.link = "";
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLisense() {
        return lisense;
    }

    public void setLisense(String lisense) {
        this.lisense = lisense;
    }

    public String getLisenseLink() {
        return lisenseLink;
    }

    public void setLisenseLink(String lisenseLink) {
        this.lisenseLink = lisenseLink;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getParentChapterId() {
        return parentChapterId;
    }

    public void setParentChapterId(int parentChapterId) {
        this.parentChapterId = parentChapterId;
    }

    public boolean isUserControlSetTop() {
        return userControlSetTop;
    }

    public void setUserControlSetTop(boolean userControlSetTop) {
        this.userControlSetTop = userControlSetTop;
    }

    public int getVisible() {
        return visible;
    }

    public void setVisible(int visible) {
        this.visible = visible;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<CategoryNodeBean> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryNodeBean> children) {
        this.children = children;
    }

    public List<ArticleListBean.Data> getArticleList() {
        return articleList;
    }

    public void setArticleList(List<ArticleListBean.Data> articleList) {
        this.articleList = articleList;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
