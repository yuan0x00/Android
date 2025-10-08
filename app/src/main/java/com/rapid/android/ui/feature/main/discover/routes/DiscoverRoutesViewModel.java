package com.rapid.android.ui.feature.main.discover.routes;

import androidx.lifecycle.MutableLiveData;

import com.rapid.android.core.data.repository.RepositoryProvider;
import com.rapid.android.core.domain.model.CategoryNodeBean;
import com.rapid.android.core.domain.repository.ContentRepository;
import com.rapid.android.core.domain.result.DomainResult;
import com.rapid.android.core.ui.presentation.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DiscoverRoutesViewModel extends BaseViewModel {

    private final ContentRepository repository = RepositoryProvider.getContentRepository();

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<List<CategoryNodeBean>> routes = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    MutableLiveData<List<CategoryNodeBean>> getRoutes() {
        return routes;
    }

    MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    void refresh() {
        loading.setValue(true);
        autoDispose(repository.popularRoutes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResult, this::handleError));
    }

    private void handleResult(DomainResult<List<CategoryNodeBean>> result) {
        loading.setValue(false);
        if (result.isSuccess() && result.getData() != null) {
            routes.setValue(result.getData());
            errorMessage.setValue(null);
        } else if (result.getError() != null && result.getError().getMessage() != null) {
            errorMessage.setValue(result.getError().getMessage());
        }
    }

    private void handleError(Throwable throwable) {
        loading.setValue(false);
        if (throwable != null && throwable.getMessage() != null) {
            errorMessage.setValue(throwable.getMessage());
        }
    }
}
