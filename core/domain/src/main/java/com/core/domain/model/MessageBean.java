package com.core.domain.model;

public class MessageBean {

    private int category;
    private String date;
    private String fromUser;
    private int fromUserId;
    private String fromUserNick;
    private int id;
    private boolean isReadState;
    private String link;
    private String message;
    private String niceDate;
    private long publishTime;
    private int tag;
    private String title;
    private int type;
    private int userId;

    public MessageBean() {
        this.category = 0;
        this.date = "";
        this.fromUser = "";
        this.fromUserId = 0;
        this.fromUserNick = "";
        this.id = 0;
        this.isReadState = false;
        this.link = "";
        this.message = "";
        this.niceDate = "";
        this.publishTime = 0L;
        this.tag = 0;
        this.title = "";
        this.type = 0;
        this.userId = 0;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public int getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(int fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getFromUserNick() {
        return fromUserNick;
    }

    public void setFromUserNick(String fromUserNick) {
        this.fromUserNick = fromUserNick;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isReadState() {
        return isReadState;
    }

    public void setReadState(boolean readState) {
        isReadState = readState;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNiceDate() {
        return niceDate;
    }

    public void setNiceDate(String niceDate) {
        this.niceDate = niceDate;
    }

    public long getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(long publishTime) {
        this.publishTime = publishTime;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
}
