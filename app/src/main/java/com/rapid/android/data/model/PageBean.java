package com.rapid.android.data.model;

import androidx.annotation.Keep;

import java.util.ArrayList;
import java.util.List;

@Keep
public class PageBean<T> {

    private int curPage;
    private List<T> datas;
    private int offset;
    private boolean over;
    private int pageCount;
    private int size;
    private int total;

    public PageBean() {
        this.curPage = 0;
        this.datas = new ArrayList<>();
        this.offset = 0;
        this.over = false;
        this.pageCount = 0;
        this.size = 0;
        this.total = 0;
    }

    public PageBean(int curPage, List<T> datas, int offset, boolean over, int pageCount, int size, int total) {
        this.curPage = curPage;
        this.datas = datas != null ? datas : new ArrayList<>();
        this.offset = offset;
        this.over = over;
        this.pageCount = pageCount;
        this.size = size;
        this.total = total;
    }

    public int getCurPage() {
        return curPage;
    }

    public void setCurPage(int curPage) {
        this.curPage = curPage;
    }

    public List<T> getDatas() {
        return datas;
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public boolean isOver() {
        return over;
    }

    public void setOver(boolean over) {
        this.over = over;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
