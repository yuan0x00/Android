package com.rapid.android.ui.feature.main.navigation;

import androidx.lifecycle.MutableLiveData;

import com.core.ui.presentation.BaseViewModel;
import com.rapid.android.data.repository.RepositoryProvider;
import com.rapid.android.domain.model.NavigationBean;
import com.rapid.android.domain.repository.ContentRepository;
import com.rapid.android.domain.result.DomainError;
import com.rapid.android.domain.result.DomainResult;

import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class NavigationViewModel extends BaseViewModel {

    private final MutableLiveData<List<NavigationBean>> navigationItems = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final ContentRepository repository = RepositoryProvider.getContentRepository();

    public MutableLiveData<List<NavigationBean>> getNavigationItems() {
        return navigationItems;
    }

    public MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadNavigation(boolean forceRefresh) {
        loading.setValue(true);
        autoDispose(repository.navigation()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<DomainResult<List<NavigationBean>>>() {
                    @Override
                    public void onNext(DomainResult<List<NavigationBean>> result) {
                        if (result.isSuccess() && result.getData() != null) {
                            navigationItems.setValue(result.getData());
                        } else {
                            DomainError error = result.getError();
                            errorMessage.setValue(error != null ? error.getMessage() : "加载导航数据失败");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        loading.setValue(false);
                        errorMessage.setValue(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        loading.setValue(false);
                    }
                }));
    }
}
