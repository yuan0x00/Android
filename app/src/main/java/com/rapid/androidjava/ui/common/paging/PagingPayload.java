package com.rapid.android.ui.common.paging;

import java.util.Collections;
import java.util.List;

public final class PagingPayload<T> {

    private final List<T> items;
    private final int nextPage;
    private final boolean hasMore;

    public PagingPayload(List<T> items, int nextPage, boolean hasMore) {
        this.items = items != null ? items : Collections.emptyList();
        this.nextPage = nextPage;
        this.hasMore = hasMore;
    }

    public List<T> getItems() {
        return items;
    }

    public int getNextPage() {
        return nextPage;
    }

    public boolean hasMore() {
        return hasMore;
    }
}
