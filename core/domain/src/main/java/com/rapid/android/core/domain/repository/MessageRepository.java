package com.rapid.android.core.domain.repository;

import com.rapid.android.core.domain.model.MessageBean;
import com.rapid.android.core.domain.model.PageBean;
import com.rapid.android.core.domain.result.DomainResult;

import io.reactivex.rxjava3.core.Observable;

public interface MessageRepository {

    Observable<DomainResult<Integer>> unreadCount();

    Observable<DomainResult<PageBean<MessageBean>>> unreadMessages(int page, Integer pageSize);

    Observable<DomainResult<PageBean<MessageBean>>> readMessages(int page, Integer pageSize);

}
