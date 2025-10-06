package com.rapid.android.ui.common.paging;

import com.core.domain.result.DomainResult;

import io.reactivex.rxjava3.core.Observable;

@FunctionalInterface
public interface PageFetcher<T> {
    Observable<DomainResult<PagingPayload<T>>> fetch(int page);
}
