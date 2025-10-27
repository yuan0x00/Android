package com.rapid.android.feature.main.home.recommend;

import com.rapid.android.core.domain.model.CategoryNodeBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PopularSection {

    private final String title;
    private final List<CategoryNodeBean> chapters;

    PopularSection(String title, List<CategoryNodeBean> chapters) {
        this.title = title != null ? title : "";
        if (chapters != null) {
            this.chapters = new ArrayList<>(chapters);
        } else {
            this.chapters = new ArrayList<>();
        }
    }

    String getTitle() {
        return title;
    }

    List<CategoryNodeBean> getChapters() {
        return Collections.unmodifiableList(chapters);
    }
}
