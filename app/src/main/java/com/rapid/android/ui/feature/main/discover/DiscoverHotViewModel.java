package com.rapid.android.ui.feature.main.discover;

import androidx.lifecycle.MutableLiveData;

import com.rapid.android.core.data.repository.RepositoryProvider;
import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.domain.model.CategoryNodeBean;
import com.rapid.android.core.domain.model.PopularColumnBean;
import com.rapid.android.core.domain.repository.ContentRepository;
import com.rapid.android.core.domain.result.DomainResult;
import com.rapid.android.core.ui.presentation.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DiscoverHotViewModel extends BaseViewModel {

    private final ContentRepository repository = RepositoryProvider.getContentRepository();

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<List<ArticleListBean.Data>> popularWenda = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<PopularColumnBean>> popularColumns = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<CategoryNodeBean>> popularRoutes = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    MutableLiveData<List<ArticleListBean.Data>> getPopularWenda() {
        return popularWenda;
    }

    MutableLiveData<List<PopularColumnBean>> getPopularColumns() {
        return popularColumns;
    }

    MutableLiveData<List<CategoryNodeBean>> getPopularRoutes() {
        return popularRoutes;
    }

    MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    void refresh() {
        loading.setValue(true);

        autoDispose(repository.popularWenda()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> handleArticleResult(result, popularWenda), throwable -> handleThrowable(throwable)));

        autoDispose(repository.popularColumns()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> handleColumnResult(result), this::handleThrowable));

        autoDispose(repository.popularRoutes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> handleRouteResult(result), this::handleThrowable));
    }

    private void handleArticleResult(DomainResult<List<ArticleListBean.Data>> result,
                                     MutableLiveData<List<ArticleListBean.Data>> target) {
        if (result.isSuccess() && result.getData() != null) {
            target.setValue(result.getData());
        }
        loading.setValue(false);
    }

    private void handleColumnResult(DomainResult<List<PopularColumnBean>> result) {
        if (result.isSuccess() && result.getData() != null) {
            popularColumns.setValue(result.getData());
        }
    }

    private void handleRouteResult(DomainResult<List<CategoryNodeBean>> result) {
        if (result.isSuccess() && result.getData() != null) {
            popularRoutes.setValue(result.getData());
        }
    }

    private void handleThrowable(Throwable throwable) {
        loading.setValue(false);
        if (throwable != null && throwable.getMessage() != null) {
            errorMessage.setValue(throwable.getMessage());
        }
    }
}
