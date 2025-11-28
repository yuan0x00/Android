package com.rapid.android.feature.main.mine.tools;

import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;

import com.rapid.android.R;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.data.repository.RepositoryProvider;
import com.rapid.android.core.domain.model.UserToolBean;
import com.rapid.android.core.domain.repository.UserRepository;
import com.rapid.android.core.domain.result.DomainError;
import com.rapid.android.core.domain.result.DomainResult;
import com.rapid.android.core.ui.presentation.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UserToolsViewModel extends BaseViewModel {

    private final UserRepository repository = RepositoryProvider.getUserRepository();

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<List<UserToolBean>> tools = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> message = new MutableLiveData<>();

    MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    MutableLiveData<List<UserToolBean>> getTools() {
        return tools;
    }

    MutableLiveData<String> getMessage() {
        return message;
    }

    void refresh() {
        loading.setValue(true);
        autoDispose(repository.userTools()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    loading.setValue(false);
                    if (result.isSuccess() && result.getData() != null) {
                        tools.setValue(result.getData());
                    } else {
                        emitError(result.getError());
                    }
                }, throwable -> {
                    loading.setValue(false);
                    emitThrowable(throwable);
                }));
    }

    void addTool(String name, String link) {
        if (!validateInput(name, link)) {
            return;
        }
        loading.setValue(true);
        autoDispose(repository.addUserTool(name.trim(), link.trim())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> handleMutationResult(result, R.string.user_tools_operation_success), this::emitThrowable));
    }

    void updateTool(int id, String name, String link) {
        if (!validateInput(name, link)) {
            return;
        }
        loading.setValue(true);
        autoDispose(repository.updateUserTool(id, name.trim(), link.trim())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> handleMutationResult(result, R.string.user_tools_operation_success), this::emitThrowable));
    }

    void deleteTool(int id) {
        loading.setValue(true);
        autoDispose(repository.deleteUserTool(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> handleMutationResult(result, R.string.user_tools_operation_success), this::emitThrowable));
    }

    private boolean validateInput(String name, String link) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(link)) {
            message.setValue(BaseApplication.getAppContext().getString(R.string.user_tools_error_empty));
            return false;
        }
        if (!link.startsWith("http://") && !link.startsWith("https://")) {
            message.setValue(BaseApplication.getAppContext().getString(R.string.user_tools_error_generic));
            return false;
        }
        return true;
    }

    private <T> void handleMutationResult(DomainResult<T> result, int successMsgRes) {
        loading.setValue(false);
        if (result.isSuccess()) {
            message.setValue(BaseApplication.getAppContext().getString(successMsgRes));
            refresh();
        } else {
            emitError(result.getError());
        }
    }

    private void emitError(DomainError error) {
        if (error != null && !TextUtils.isEmpty(error.getMessage())) {
            message.setValue(error.getMessage());
        } else {
            message.setValue(BaseApplication.getAppContext().getString(R.string.user_tools_error_generic));
        }
    }

    private void emitThrowable(Throwable throwable) {
        loading.setValue(false);
        if (throwable != null && !TextUtils.isEmpty(throwable.getMessage())) {
            message.setValue(throwable.getMessage());
        } else {
            message.setValue(BaseApplication.getAppContext().getString(R.string.user_tools_error_link));
        }
    }
}
