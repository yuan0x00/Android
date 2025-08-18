package com.example.android.bean;

import androidx.annotation.Keep;

import java.util.ArrayList;
import java.util.List;

@Keep
public class CollectBean {
    private int curPage;
    private List<Data> datas;
    private int offset;
    private boolean over;
    private int pageCount;
    private int size;
    private int total;

    public CollectBean() {
        this.curPage = 0;
        this.datas = new ArrayList<>();
        this.offset = 0;
        this.over = false;
        this.pageCount = 0;
        this.size = 0;
        this.total = 0;
    }

    public CollectBean(int curPage, List<Data> datas, int offset, boolean over,
                       int pageCount, int size, int total) {
        this.curPage = curPage;
        this.datas = datas != null ? datas : new ArrayList<>();
        this.offset = offset;
        this.over = over;
        this.pageCount = pageCount;
        this.size = size;
        this.total = total;
    }

    // Getters
    public int getCurPage() {
        return curPage;
    }

    public List<Data> getDatas() {
        return datas;
    }

    public int getOffset() {
        return offset;
    }

    public boolean isOver() {
        return over;
    }

    public int getPageCount() {
        return pageCount;
    }

    public int getSize() {
        return size;
    }

    public int getTotal() {
        return total;
    }

    // Setters
    public void setCurPage(int curPage) {
        this.curPage = curPage;
    }

    public void setDatas(List<Data> datas) {
        this.datas = datas;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setOver(boolean over) {
        this.over = over;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Keep
    public static class Data {
        private String author;
        private int chapterId;
        private String chapterName;
        private int courseId;
        private String desc;
        private String envelopePic;
        private int id;
        private String link;
        private String niceDate;
        private String origin;
        private int originId;
        private long publishTime;
        private String title;
        private int userId;
        private int visible;
        private int zan;

        public Data() {
            this.author = "";
            this.chapterId = 0;
            this.chapterName = "";
            this.courseId = 0;
            this.desc = "";
            this.envelopePic = "";
            this.id = 0;
            this.link = "";
            this.niceDate = "";
            this.origin = "";
            this.originId = 0;
            this.publishTime = 0;
            this.title = "";
            this.userId = 0;
            this.visible = 0;
            this.zan = 0;
        }

        // Getters
        public String getAuthor() {
            return author;
        }

        public int getChapterId() {
            return chapterId;
        }

        public String getChapterName() {
            return chapterName;
        }

        public int getCourseId() {
            return courseId;
        }

        public String getDesc() {
            return desc;
        }

        public String getEnvelopePic() {
            return envelopePic;
        }

        public int getId() {
            return id;
        }

        public String getLink() {
            return link;
        }

        public String getNiceDate() {
            return niceDate;
        }

        public String getOrigin() {
            return origin;
        }

        public int getOriginId() {
            return originId;
        }

        public long getPublishTime() {
            return publishTime;
        }

        public String getTitle() {
            return title;
        }

        public int getUserId() {
            return userId;
        }

        public int getVisible() {
            return visible;
        }

        public int getZan() {
            return zan;
        }

        // Setters
        public void setAuthor(String author) {
            this.author = author;
        }

        public void setChapterId(int chapterId) {
            this.chapterId = chapterId;
        }

        public void setChapterName(String chapterName) {
            this.chapterName = chapterName;
        }

        public void setCourseId(int courseId) {
            this.courseId = courseId;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public void setEnvelopePic(String envelopePic) {
            this.envelopePic = envelopePic;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public void setNiceDate(String niceDate) {
            this.niceDate = niceDate;
        }

        public void setOrigin(String origin) {
            this.origin = origin;
        }

        public void setOriginId(int originId) {
            this.originId = originId;
        }

        public void setPublishTime(long publishTime) {
            this.publishTime = publishTime;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public void setVisible(int visible) {
            this.visible = visible;
        }

        public void setZan(int zan) {
            this.zan = zan;
        }
    }
}