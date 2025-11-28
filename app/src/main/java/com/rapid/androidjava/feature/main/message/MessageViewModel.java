package com.rapid.android.feature.main.message;

import androidx.lifecycle.MutableLiveData;

import com.rapid.android.R;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.data.repository.RepositoryProvider;
import com.rapid.android.core.domain.repository.MessageRepository;
import com.rapid.android.core.domain.result.DomainError;
import com.rapid.android.core.domain.result.DomainResult;
import com.rapid.android.core.ui.presentation.BaseViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MessageViewModel extends BaseViewModel {

    private final MessageRepository repository = RepositoryProvider.getMessageRepository();
    private final MutableLiveData<Integer> unreadCount = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    MutableLiveData<Integer> getUnreadCount() {
        return unreadCount;
    }

    MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    void refreshUnreadCount() {
        loading.setValue(true);
        autoDispose(repository.unreadCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleUnreadResult, this::handleUnreadError));
    }

    private void handleUnreadResult(DomainResult<Integer> result) {
        loading.setValue(false);
        if (result.isSuccess() && result.getData() != null) {
            unreadCount.setValue(result.getData());
            return;
        }
        DomainError error = result.getError();
        String message = error != null && error.getMessage() != null
                ? error.getMessage()
                : BaseApplication.getAppContext().getString(R.string.message_unread_count_error);
        errorMessage.setValue(message);
    }

    private void handleUnreadError(Throwable throwable) {
        loading.setValue(false);
        String message = throwable != null && throwable.getMessage() != null
                ? throwable.getMessage()
                : BaseApplication.getAppContext().getString(R.string.message_unread_count_error);
        errorMessage.setValue(message);
    }
}
