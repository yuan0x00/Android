package com.rapid.android.ui.feature.main.navigation;

import androidx.lifecycle.MutableLiveData;

import com.core.common.app.BaseApplication;
import com.core.data.repository.RepositoryProvider;
import com.core.domain.model.NavigationBean;
import com.core.domain.repository.ContentRepository;
import com.core.domain.result.DomainError;
import com.core.domain.result.DomainResult;
import com.core.ui.presentation.BaseViewModel;
import com.rapid.android.R;

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
                            if (error != null && error.getMessage() != null) {
                                errorMessage.setValue(error.getMessage());
                            } else {
                                errorMessage.setValue(BaseApplication.getAppContext()
                                        .getString(R.string.navigation_error_load_failed));
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        loading.setValue(false);
                        errorMessage.setValue(e != null && e.getMessage() != null
                                ? e.getMessage()
                                : BaseApplication.getAppContext().getString(R.string.navigation_error_load_failed));
                    }

                    @Override
                    public void onComplete() {
                        loading.setValue(false);
                    }
                }));
    }
}
