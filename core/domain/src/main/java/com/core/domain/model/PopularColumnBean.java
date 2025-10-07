package com.core.domain.model;

public class PopularColumnBean {

    private int chapterId;
    private String chapterName;
    private int columnId;
    private int id;
    private String name;
    private int subChapterId;
    private String subChapterName;
    private String url;
    private int userId;

    public PopularColumnBean() {
        this.chapterId = 0;
        this.chapterName = "";
        this.columnId = 0;
        this.id = 0;
        this.name = "";
        this.subChapterId = 0;
        this.subChapterName = "";
        this.url = "";
        this.userId = 0;
    }

    public int getChapterId() {
        return chapterId;
    }

    public void setChapterId(int chapterId) {
        this.chapterId = chapterId;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public int getColumnId() {
        return columnId;
    }

    public void setColumnId(int columnId) {
        this.columnId = columnId;
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

    public int getSubChapterId() {
        return subChapterId;
    }

    public void setSubChapterId(int subChapterId) {
        this.subChapterId = subChapterId;
    }

    public String getSubChapterName() {
        return subChapterName;
    }

    public void setSubChapterName(String subChapterName) {
        this.subChapterName = subChapterName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
