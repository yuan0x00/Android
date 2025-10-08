package com.rapid.android.ui.feature.main.discover.tool;

import androidx.lifecycle.MutableLiveData;

import com.rapid.android.R;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.data.repository.RepositoryProvider;
import com.rapid.android.core.domain.model.ToolItemBean;
import com.rapid.android.core.domain.repository.HomeRepository;
import com.rapid.android.core.domain.result.DomainError;
import com.rapid.android.core.domain.result.DomainResult;
import com.rapid.android.core.ui.presentation.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ToolCenterViewModel extends BaseViewModel {

    private final HomeRepository repository = RepositoryProvider.getHomeRepository();
    private final MutableLiveData<List<ToolItemBean>> tools = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public MutableLiveData<List<ToolItemBean>> getTools() {
        return tools;
    }

    public MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void refresh() {
        loading.setValue(true);
        autoDispose(repository.toolList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResult, this::handleError));
    }

    private void handleResult(DomainResult<List<ToolItemBean>> result) {
        loading.setValue(false);
        if (result.isSuccess() && result.getData() != null) {
            tools.setValue(result.getData());
            return;
        }
        DomainError error = result.getError();
        String message = error != null && error.getMessage() != null
                ? error.getMessage()
                : BaseApplication.getAppContext().getString(R.string.tool_center_load_failed);
        errorMessage.setValue(message);
    }

    private void handleError(Throwable throwable) {
        loading.setValue(false);
        String message = throwable != null && throwable.getMessage() != null
                ? throwable.getMessage()
                : BaseApplication.getAppContext().getString(R.string.tool_center_load_failed);
        errorMessage.setValue(message);
    }
}
