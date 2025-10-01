package com.rapid.android.ui.feature.main.system;

import androidx.lifecycle.MutableLiveData;

import com.core.ui.presentation.BaseViewModel;
import com.rapid.android.data.repository.RepositoryProvider;
import com.rapid.android.domain.model.CategoryNodeBean;
import com.rapid.android.domain.repository.ContentRepository;
import com.rapid.android.domain.result.DomainError;
import com.rapid.android.domain.result.DomainResult;

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
                            errorMessage.setValue(error != null ? error.getMessage() : "加载体系数据失败");
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
