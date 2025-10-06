package com.rapid.android.ui.common.paging;

import androidx.lifecycle.MutableLiveData;

import com.core.domain.result.DomainError;
import com.core.domain.result.DomainResult;
import com.core.ui.presentation.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.Disposable;

public final class PagingController<T> {

    private final BaseViewModel owner;
    private final int firstPage;
    private final PageFetcher<T> fetcher;

    private final List<T> internalItems = new ArrayList<>();

    private final MutableLiveData<List<T>> itemsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> loadingMoreLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> hasMoreLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private int nextPage;
    private boolean initialized = false;

    public PagingController(BaseViewModel owner, int firstPage, PageFetcher<T> fetcher) {
        this.owner = owner;
        this.firstPage = firstPage;
        this.fetcher = fetcher;
        this.nextPage = firstPage;
    }

    public MutableLiveData<List<T>> getItemsLiveData() {
        return itemsLiveData;
    }

    public MutableLiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public MutableLiveData<Boolean> getLoadingMoreLiveData() {
        return loadingMoreLiveData;
    }

    public MutableLiveData<Boolean> getHasMoreLiveData() {
        return hasMoreLiveData;
    }

    public MutableLiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void refresh() {
        nextPage = firstPage;
        hasMoreLiveData.setValue(true);
        load(true);
    }

    public void loadMore() {
        if (!Boolean.TRUE.equals(hasMoreLiveData.getValue())) {
            return;
        }
        if (Boolean.TRUE.equals(loadingLiveData.getValue()) || Boolean.TRUE.equals(loadingMoreLiveData.getValue())) {
            return;
        }
        load(false);
    }

    public boolean isInitialized() {
        return initialized;
    }

    private void load(boolean refresh) {
        initialized = true;
        if (refresh) {
            loadingLiveData.setValue(true);
        } else {
            loadingMoreLiveData.setValue(true);
        }

        Disposable disposable = fetcher.fetch(nextPage)
                .subscribe(result -> handleResult(result, refresh), this::handleError);
        owner.trackDisposable(disposable);
    }

    private void handleResult(DomainResult<PagingPayload<T>> result, boolean refresh) {
        loadingLiveData.setValue(false);
        loadingMoreLiveData.setValue(false);

        if (result.isSuccess() && result.getData() != null) {
            PagingPayload<T> payload = result.getData();
            if (refresh) {
                internalItems.clear();
            }
            internalItems.addAll(payload.getItems());
            itemsLiveData.setValue(new ArrayList<>(internalItems));

            nextPage = payload.getNextPage();
            hasMoreLiveData.setValue(payload.hasMore());
        } else {
            DomainError error = result.getError();
            if (error != null) {
                errorLiveData.setValue(error.getMessage());
            }
        }
    }

    private void handleError(Throwable throwable) {
        loadingLiveData.setValue(false);
        loadingMoreLiveData.setValue(false);
        errorLiveData.setValue(throwable.getMessage());
    }
}
