package com.rapid.android.feature.search;

import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;

import com.rapid.android.R;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.data.repository.RepositoryProvider;
import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.domain.model.HotKeyBean;
import com.rapid.android.core.domain.repository.ContentRepository;
import com.rapid.android.core.domain.result.DomainError;
import com.rapid.android.core.domain.result.DomainResult;
import com.rapid.android.core.ui.presentation.BaseViewModel;
import com.rapid.android.ui.common.paging.PagingController;
import com.rapid.android.ui.common.paging.PagingPayload;
import com.rapid.android.utils.SearchHistoryStore;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SearchViewModel extends BaseViewModel {

    private final ContentRepository repository = RepositoryProvider.getContentRepository();
    private final MutableLiveData<List<HotKeyBean>> hotKeys = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> histories = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showSuggestions = new MutableLiveData<>(true);
    private String currentKeyword = "";
    private final PagingController<ArticleListBean.Data> pagingController =
            new PagingController<>(this, 0, this::fetchSearchPage);

    public MutableLiveData<List<HotKeyBean>> getHotKeys() {
        return hotKeys;
    }

    public MutableLiveData<List<String>> getHistories() {
        return histories;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<Boolean> getShowSuggestions() {
        return showSuggestions;
    }

    public MutableLiveData<List<ArticleListBean.Data>> getSearchResults() {
        return pagingController.getItemsLiveData();
    }

    public MutableLiveData<Boolean> getLoading() {
        return pagingController.getLoadingLiveData();
    }

    public MutableLiveData<Boolean> getLoadingMore() {
        return pagingController.getLoadingMoreLiveData();
    }

    public MutableLiveData<Boolean> getHasMore() {
        return pagingController.getHasMoreLiveData();
    }

    public MutableLiveData<String> getPagingError() {
        return pagingController.getErrorLiveData();
    }

    public MutableLiveData<Boolean> getEmptyState() {
        return pagingController.getEmptyStateLiveData();
    }

    public void initialize() {
        loadHistories();
        loadHotKeys();
        showSuggestions.setValue(true);
    }

    public void refreshHotKeys() {
        loadHotKeys();
    }

    public void clearHistory() {
        SearchHistoryStore.clearHistory();
        loadHistories();
    }

    public void submitSearch(String keyword) {
        String target = keyword != null ? keyword.trim() : "";
        if (TextUtils.isEmpty(target)) {
            currentKeyword = "";
            showSuggestions.setValue(true);
            pagingController.refresh();
            return;
        }
        if (target.equals(currentKeyword)) {
            showSuggestions.setValue(false);
            return;
        }
        currentKeyword = target;
        SearchHistoryStore.addHistory(target);
        loadHistories();
        showSuggestions.setValue(false);
        pagingController.refresh();
    }

    public void loadMore() {
        pagingController.loadMore();
    }

    public void retry() {
        if (TextUtils.isEmpty(currentKeyword)) {
            showSuggestions.setValue(true);
            return;
        }
        pagingController.refresh();
    }

    private void loadHistories() {
        histories.setValue(new ArrayList<>(SearchHistoryStore.getHistories()));
    }

    private void loadHotKeys() {
        autoDispose(repository.hotKeys()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        hotKeys.setValue(result.getData());
                    } else {
                        DomainError error = result.getError();
                        if (error != null && error.getMessage() != null) {
                            errorMessage.setValue(error.getMessage());
                        }
                    }
                }, throwable -> {
                    if (throwable != null && throwable.getMessage() != null) {
                        errorMessage.setValue(throwable.getMessage());
                    }
                }));
    }

    private Observable<DomainResult<PagingPayload<ArticleListBean.Data>>> fetchSearchPage(int page) {
        if (TextUtils.isEmpty(currentKeyword)) {
            return Observable.just(DomainResult.success(new PagingPayload<>(new ArrayList<>(), page, false)));
        }
        return repository.searchArticles(page, currentKeyword)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        ArticleListBean listBean = result.getData();
                        int next = listBean.getCurPage();
                        boolean hasMore = !listBean.isOver();
                        List<ArticleListBean.Data> items = listBean.getDatas();
                        if (items == null) {
                            items = new ArrayList<>();
                        }
                        return DomainResult.success(new PagingPayload<>(items, next, hasMore));
                    }
                    DomainError error = result.getError();
                    String message = error != null && error.getMessage() != null
                            ? error.getMessage()
                            : BaseApplication.getAppContext().getString(R.string.home_article_load_failed);
                    errorMessage.setValue(message);
                    return DomainResult.failure(error != null ? error
                            : DomainError.of(DomainError.UNKNOWN_CODE, message));
                });
    }
}
