package com.rapid.android.ui.feature.main.discover.system;

import androidx.lifecycle.MutableLiveData;

import com.rapid.android.R;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.data.repository.RepositoryProvider;
import com.rapid.android.core.domain.model.CategoryNodeBean;
import com.rapid.android.core.domain.repository.ContentRepository;
import com.rapid.android.core.domain.result.DomainError;
import com.rapid.android.core.domain.result.DomainResult;
import com.rapid.android.core.ui.presentation.BaseViewModel;

import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SystemViewModel extends BaseViewModel {

    private final MutableLiveData<List<CategoryNodeBean>> categories = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final ContentRepository repository = RepositoryProvider.getContentRepository();

    public MutableLiveData<List<CategoryNodeBean>> getCategories() {
        return categories;
    }

    public MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadSystem(boolean forceRefresh) {
        loading.setValue(true);
        autoDispose(repository.knowledgeTree()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<DomainResult<List<CategoryNodeBean>>>() {
                    @Override
                    public void onNext(DomainResult<List<CategoryNodeBean>> result) {
                        if (result.isSuccess() && result.getData() != null) {
                            categories.setValue(result.getData());
                        } else {
                            DomainError error = result.getError();
                            if (error != null && error.getMessage() != null) {
                                errorMessage.setValue(error.getMessage());
                            } else {
                                errorMessage.setValue(BaseApplication.getAppContext()
                                        .getString(R.string.system_error_load_failed));
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        loading.setValue(false);
                        errorMessage.setValue(e != null && e.getMessage() != null
                                ? e.getMessage()
                                : BaseApplication.getAppContext().getString(R.string.system_error_load_failed));
                    }

                    @Override
                    public void onComplete() {
                        loading.setValue(false);
                    }
                }));
    }
}
