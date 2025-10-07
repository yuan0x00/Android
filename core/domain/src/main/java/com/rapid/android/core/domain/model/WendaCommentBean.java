package com.rapid.android.core.domain.model;

import java.util.ArrayList;
import java.util.List;

public class WendaCommentBean {

    private int anonymous;
    private int appendForContent;
    private int articleId;
    private boolean canEdit;
    private String content;
    private String contentMd;
    private int id;
    private String niceDate;
    private long publishDate;
    private int replyCommentId;
    private List<WendaCommentBean> replyComments;
    private int rootCommentId;
    private int status;
    private int toUserId;
    private String toUserName;
    private int userId;
    private String userName;
    private int zan;

    public WendaCommentBean() {
        this.anonymous = 0;
        this.appendForContent = 0;
        this.articleId = 0;
        this.canEdit = false;
        this.content = "";
        this.contentMd = "";
        this.id = 0;
        this.niceDate = "";
        this.publishDate = 0L;
        this.replyCommentId = 0;
        this.replyComments = new ArrayList<>();
        this.rootCommentId = 0;
        this.status = 0;
        this.toUserId = 0;
        this.toUserName = "";
        this.userId = 0;
        this.userName = "";
        this.zan = 0;
    }

    public int getAnonymous() {
        return anonymous;
    }

    public void setAnonymous(int anonymous) {
        this.anonymous = anonymous;
    }

    public int getAppendForContent() {
        return appendForContent;
    }

    public void setAppendForContent(int appendForContent) {
        this.appendForContent = appendForContent;
    }

    public int getArticleId() {
        return articleId;
    }

    public void setArticleId(int articleId) {
        this.articleId = articleId;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentMd() {
        return contentMd;
    }

    public void setContentMd(String contentMd) {
        this.contentMd = contentMd;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNiceDate() {
        return niceDate;
    }

    public void setNiceDate(String niceDate) {
        this.niceDate = niceDate;
    }

    public long getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(long publishDate) {
        this.publishDate = publishDate;
    }

    public int getReplyCommentId() {
        return replyCommentId;
    }

    public void setReplyCommentId(int replyCommentId) {
        this.replyCommentId = replyCommentId;
    }

    public List<WendaCommentBean> getReplyComments() {
        return replyComments;
    }

    public void setReplyComments(List<WendaCommentBean> replyComments) {
        this.replyComments = replyComments;
    }

    public int getRootCommentId() {
        return rootCommentId;
    }

    public void setRootCommentId(int rootCommentId) {
        this.rootCommentId = rootCommentId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getToUserId() {
        return toUserId;
    }

    public void setToUserId(int toUserId) {
        this.toUserId = toUserId;
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getZan() {
        return zan;
    }

    public void setZan(int zan) {
        this.zan = zan;
    }
}
