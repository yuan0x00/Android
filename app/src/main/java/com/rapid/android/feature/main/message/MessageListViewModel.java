package com.rapid.android.feature.main.message;

import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rapid.android.R;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.data.repository.RepositoryProvider;
import com.rapid.android.core.domain.model.MessageBean;
import com.rapid.android.core.domain.model.PageBean;
import com.rapid.android.core.domain.repository.MessageRepository;
import com.rapid.android.core.domain.result.DomainError;
import com.rapid.android.core.domain.result.DomainResult;
import com.rapid.android.core.ui.presentation.BaseViewModel;
import com.rapid.android.ui.common.paging.PagingController;
import com.rapid.android.ui.common.paging.PagingPayload;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

class MessageListViewModel extends BaseViewModel {

    private final MessageCategory category;
    private final MessageRepository repository = RepositoryProvider.getMessageRepository();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final PagingController<MessageBean> pagingController =
            new PagingController<>(this, 1, this::fetchPage);
    private final MutableLiveData<Long> unreadSyncSignal = new MutableLiveData<>();

    MessageListViewModel(MessageCategory category) {
        this.category = category;
    }

    MutableLiveData<List<MessageBean>> getMessages() {
        return pagingController.getItemsLiveData();
    }

    MutableLiveData<Boolean> getLoading() {
        return pagingController.getLoadingLiveData();
    }

    MutableLiveData<Boolean> getLoadingMore() {
        return pagingController.getLoadingMoreLiveData();
    }

    MutableLiveData<Boolean> getHasMore() {
        return pagingController.getHasMoreLiveData();
    }

    MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    MutableLiveData<Long> getUnreadSyncSignal() {
        return unreadSyncSignal;
    }

    MutableLiveData<Boolean> getEmptyState() {
        return pagingController.getEmptyStateLiveData();
    }

    void initialize() {
        if (!pagingController.isInitialized()) {
            pagingController.refresh();
        }
    }

    void refresh() {
        pagingController.refresh();
    }

    void loadMore() {
        pagingController.loadMore();
    }

    private Observable<DomainResult<PagingPayload<MessageBean>>> fetchPage(int page) {
        Observable<DomainResult<PageBean<MessageBean>>> source;
        if (category == MessageCategory.READ) {
            source = repository.readMessages(page, null);
        } else {
            source = repository.unreadMessages(page, null);
        }

        return source
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        PageBean<MessageBean> pageBean = result.getData();
                        List<MessageBean> items = pageBean.getDatas();
                        if (items == null) {
                            items = new ArrayList<>();
                        }
                        int nextPage = pageBean.getCurPage() + 1;
                        boolean hasMore = !pageBean.isOver() && pageBean.getCurPage() < pageBean.getPageCount();
                        if (category == MessageCategory.UNREAD && page == 1) {
                            unreadSyncSignal.setValue(SystemClock.uptimeMillis());
                        }
                        return DomainResult.success(new PagingPayload<>(items, nextPage, hasMore));
                    }

                    DomainError error = result.getError();
                    String message = error != null && error.getMessage() != null
                            ? error.getMessage()
                            : BaseApplication.getAppContext().getString(R.string.message_list_error);
                    errorMessage.setValue(message);
                    return DomainResult.failure(error != null ? error
                            : DomainError.of(DomainError.UNKNOWN_CODE, message));
                });
    }

    static class Factory implements ViewModelProvider.Factory {

        private final MessageCategory category;

        Factory(@NonNull MessageCategory category) {
            this.category = category;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(MessageListViewModel.class)) {
                return (T) new MessageListViewModel(category);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
