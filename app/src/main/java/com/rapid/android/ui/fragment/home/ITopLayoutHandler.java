package com.rapid.android.ui.fragment.home;

public interface ITopLayoutHandler {

    /**
     * 顶部区域是否折叠
     */
    boolean isTopLayoutCollapsed();

    /**
     * 设置顶部区域折叠
     */
    void setTopLayoutCollapsed(Boolean isCollapsed);

}
