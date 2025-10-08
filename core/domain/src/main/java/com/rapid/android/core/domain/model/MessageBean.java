package com.rapid.android.core.domain.model;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MessageBean {

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    );
    @SerializedName("category")
    private int category;
    @SerializedName("date")
    private long date;
    @SerializedName("fromUser")
    private String fromUser;
    @SerializedName("fromUserId")
    private int fromUserId;
    @SerializedName("fromUserNick")
    private String fromUserNick;
    @SerializedName("fullLink")
    private String fullLink;
    @SerializedName("id")
    private int id;
    @SerializedName("isRead")
    private int readFlag;
    @SerializedName("link")
    private String link;
    @SerializedName("message")
    private String message;
    @SerializedName("niceDate")
    private String niceDate;
    @SerializedName("tag")
    private String tag;
    @SerializedName("title")
    private String title;
    @SerializedName("type")
    private int type;
    @SerializedName("userId")
    private int userId;

    public MessageBean() {
        this.category = 0;
        this.date = 0L;
        this.fromUser = "";
        this.fromUserId = 0;
        this.fromUserNick = "";
        this.fullLink = "";
        this.id = 0;
        this.readFlag = 0;
        this.link = "";
        this.message = "";
        this.niceDate = "";
        this.tag = "";
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

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
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

    public String getFullLink() {
        return fullLink;
    }

    public void setFullLink(String fullLink) {
        this.fullLink = fullLink;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReadFlag() {
        return readFlag;
    }

    public void setReadFlag(int readFlag) {
        this.readFlag = readFlag;
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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
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

    public boolean isRead() {
        return readFlag == 1;
    }

    public String getDisplayTime() {
        if (niceDate != null && !niceDate.isEmpty()) {
            return niceDate;
        }
        if (date <= 0) {
            return "";
        }
        Date target = new Date(date);
        return DATE_FORMAT.get().format(target);
    }

    public String getEffectiveLink() {
        if (link != null && !link.trim().isEmpty()) {
            return link.trim();
        }
        if (fullLink != null && !fullLink.trim().isEmpty()) {
            return fullLink.trim();
        }
        return "";
    }
}
