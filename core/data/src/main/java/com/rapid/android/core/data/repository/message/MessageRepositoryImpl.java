package com.rapid.android.core.data.repository.message;

import com.rapid.android.core.data.api.MessageApi;
import com.rapid.android.core.data.mapper.DomainResultMapper;
import com.rapid.android.core.data.network.NetApis;
import com.rapid.android.core.domain.model.MessageBean;
import com.rapid.android.core.domain.model.PageBean;
import com.rapid.android.core.domain.repository.MessageRepository;
import com.rapid.android.core.domain.result.DomainResult;

import io.reactivex.rxjava3.core.Observable;

public class MessageRepositoryImpl implements MessageRepository {

    @Override
    public Observable<DomainResult<Integer>> unreadCount() {
        return api().unreadCount()
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }

    @Override
    public Observable<DomainResult<PageBean<MessageBean>>> unreadMessages(int page, Integer pageSize) {
        return api().unreadMessages(page, pageSize)
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }

    @Override
    public Observable<DomainResult<PageBean<MessageBean>>> readMessages(int page, Integer pageSize) {
        return api().readMessages(page, pageSize)
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }

    private MessageApi api() {
        return NetApis.Message();
    }
}
