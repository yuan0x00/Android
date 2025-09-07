package com.rapid.android.bean;

import androidx.annotation.Keep;

import java.util.ArrayList;
import java.util.List;

@Keep
public class ArticleListBean {
    private int curPage;
    private List<Data> datas;
    private int offset;
    private boolean over;
    private int pageCount;
    private int size;
    private int total;

    public ArticleListBean() {
        this.curPage = 0;
        this.datas = new ArrayList<>();
        this.offset = 0;
        this.over = false;
        this.pageCount = 0;
        this.size = 0;
        this.total = 0;
    }

    public ArticleListBean(int curPage, List<Data> datas, int offset, boolean over,
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
        private boolean adminAdd;
        private String apkLink;
        private int audit;
        private String author;
        private boolean canEdit;
        private int chapterId;
        private String chapterName;
        private boolean collect;
        private int courseId;
        private String desc;
        private String descMd;
        private String envelopePic;
        private boolean fresh;
        private String host;
        private int id;
        private boolean isAdminAdd;
        private String link;
        private String niceDate;
        private String niceShareDate;
        private String origin;
        private String prefix;
        private String projectLink;
        private long publishTime;
        private int realSuperChapterId;
        private int selfVisible;
        private long shareDate;
        private String shareUser;
        private int superChapterId;
        private String superChapterName;
        private List<Tag> tags;
        private String title;
        private int type;
        private int userId;
        private int visible;
        private int zan;

        public Data() {
            this.adminAdd = false;
            this.apkLink = "";
            this.audit = 0;
            this.author = "";
            this.canEdit = false;
            this.chapterId = 0;
            this.chapterName = "";
            this.collect = false;
            this.courseId = 0;
            this.desc = "";
            this.descMd = "";
            this.envelopePic = "";
            this.fresh = false;
            this.host = "";
            this.id = 0;
            this.isAdminAdd = false;
            this.link = "";
            this.niceDate = "";
            this.niceShareDate = "";
            this.origin = "";
            this.prefix = "";
            this.projectLink = "";
            this.publishTime = 0;
            this.realSuperChapterId = 0;
            this.selfVisible = 0;
            this.shareDate = 0;
            this.shareUser = "";
            this.superChapterId = 0;
            this.superChapterName = "";
            this.tags = new ArrayList<>();
            this.title = "";
            this.type = 0;
            this.userId = 0;
            this.visible = 0;
            this.zan = 0;
        }

        public String getApkLink() {
            return apkLink;
        }

        public int getAudit() {
            return audit;
        }

        public String getAuthor() {
            return author;
        }

        public boolean isCanEdit() {
            return canEdit;
        }

        public int getChapterId() {
            return chapterId;
        }

        public String getChapterName() {
            return chapterName;
        }

        public boolean isCollect() {
            return collect;
        }

        public int getCourseId() {
            return courseId;
        }

        public String getDesc() {
            return desc;
        }

        public String getDescMd() {
            return descMd;
        }

        public String getEnvelopePic() {
            return envelopePic;
        }

        public boolean isFresh() {
            return fresh;
        }

        public String getHost() {
            return host;
        }

        public int getId() {
            return id;
        }

        public boolean isAdminAdd() {
            return isAdminAdd;
        }

        public String getLink() {
            return link;
        }

        public String getNiceDate() {
            return niceDate;
        }

        public String getNiceShareDate() {
            return niceShareDate;
        }

        public String getOrigin() {
            return origin;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getProjectLink() {
            return projectLink;
        }

        public long getPublishTime() {
            return publishTime;
        }

        public int getRealSuperChapterId() {
            return realSuperChapterId;
        }

        public int getSelfVisible() {
            return selfVisible;
        }

        public long getShareDate() {
            return shareDate;
        }

        public String getShareUser() {
            return shareUser;
        }

        public int getSuperChapterId() {
            return superChapterId;
        }

        public String getSuperChapterName() {
            return superChapterName;
        }

        public List<Tag> getTags() {
            return tags;
        }

        public String getTitle() {
            return title;
        }

        public int getType() {
            return type;
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
        public void setAdminAdd(boolean adminAdd) {
            this.adminAdd = adminAdd;
        }

        public void setApkLink(String apkLink) {
            this.apkLink = apkLink;
        }

        public void setAudit(int audit) {
            this.audit = audit;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public void setCanEdit(boolean canEdit) {
            this.canEdit = canEdit;
        }

        public void setChapterId(int chapterId) {
            this.chapterId = chapterId;
        }

        public void setChapterName(String chapterName) {
            this.chapterName = chapterName;
        }

        public void setCollect(boolean collect) {
            this.collect = collect;
        }

        public void setCourseId(int courseId) {
            this.courseId = courseId;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public void setDescMd(String descMd) {
            this.descMd = descMd;
        }

        public void setEnvelopePic(String envelopePic) {
            this.envelopePic = envelopePic;
        }

        public void setFresh(boolean fresh) {
            this.fresh = fresh;
        }

        public void setHost(String host) {
            this.host = host;
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

        public void setNiceShareDate(String niceShareDate) {
            this.niceShareDate = niceShareDate;
        }

        public void setOrigin(String origin) {
            this.origin = origin;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public void setProjectLink(String projectLink) {
            this.projectLink = projectLink;
        }

        public void setPublishTime(long publishTime) {
            this.publishTime = publishTime;
        }

        public void setRealSuperChapterId(int realSuperChapterId) {
            this.realSuperChapterId = realSuperChapterId;
        }

        public void setSelfVisible(int selfVisible) {
            this.selfVisible = selfVisible;
        }

        public void setShareDate(long shareDate) {
            this.shareDate = shareDate;
        }

        public void setShareUser(String shareUser) {
            this.shareUser = shareUser;
        }

        public void setSuperChapterId(int superChapterId) {
            this.superChapterId = superChapterId;
        }

        public void setSuperChapterName(String superChapterName) {
            this.superChapterName = superChapterName;
        }

        public void setTags(List<Tag> tags) {
            this.tags = tags;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setType(int type) {
            this.type = type;
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

        @Keep
        public static class Tag {
            private String name;
            private String url;

            public Tag() {
                this.name = "";
                this.url = "";
            }

            public Tag(String name, String url) {
                this.name = name != null ? name : "";
                this.url = url != null ? url : "";
            }

            // Getters
            public String getName() {
                return name;
            }

            public String getUrl() {
                return url;
            }

            // Setters
            public void setName(String name) {
                this.name = name;
            }

            public void setUrl(String url) {
                this.url = url;
            }
        }
    }
}
