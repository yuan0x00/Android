package com.rapid.android.feature.main.discover.harmony;

import androidx.lifecycle.MutableLiveData;

import com.rapid.android.core.data.repository.RepositoryProvider;
import com.rapid.android.core.domain.model.HarmonyIndexBean;
import com.rapid.android.core.domain.repository.HomeRepository;
import com.rapid.android.core.domain.result.DomainResult;
import com.rapid.android.core.ui.presentation.BaseViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HarmonyViewModel extends BaseViewModel {

    private final HomeRepository repository = RepositoryProvider.getHomeRepository();

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<HarmonyIndexBean> harmonyIndex = new MutableLiveData<>(new HarmonyIndexBean());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    MutableLiveData<HarmonyIndexBean> getHarmonyIndex() {
        return harmonyIndex;
    }

    MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    void refresh() {
        loading.setValue(true);
        autoDispose(repository.harmonyIndex()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResult, this::handleError));
    }

    private void handleResult(DomainResult<HarmonyIndexBean> result) {
        loading.setValue(false);
        if (result.isSuccess() && result.getData() != null) {
            harmonyIndex.setValue(result.getData());
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
