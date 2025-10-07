package com.rapid.android.core.domain.model;

import com.google.gson.annotations.SerializedName;

public class HarmonyIndexBean {

    @SerializedName("links")
    private CategoryNodeBean links;

    @SerializedName("open_sources")
    private CategoryNodeBean openSources;

    @SerializedName("tools")
    private CategoryNodeBean tools;

    public HarmonyIndexBean() {
        this.links = new CategoryNodeBean();
        this.openSources = new CategoryNodeBean();
        this.tools = new CategoryNodeBean();
    }

    public CategoryNodeBean getLinks() {
        return links;
    }

    public void setLinks(CategoryNodeBean links) {
        this.links = links;
    }

    public CategoryNodeBean getOpenSources() {
        return openSources;
    }

    public void setOpenSources(CategoryNodeBean openSources) {
        this.openSources = openSources;
    }

    public CategoryNodeBean getTools() {
        return tools;
    }

    public void setTools(CategoryNodeBean tools) {
        this.tools = tools;
    }
}
