package com.rapid.android.ui.feature.share;

import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;

import com.rapid.android.R;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.data.repository.RepositoryProvider;
import com.rapid.android.core.domain.repository.UserRepository;
import com.rapid.android.core.domain.result.DomainError;
import com.rapid.android.core.ui.presentation.BaseViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ShareArticleViewModel extends BaseViewModel {

    private final UserRepository repository = RepositoryProvider.getUserRepository();

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> submitEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> submitSuccess = new MutableLiveData<>(false);

    private String currentTitle = "";
    private String currentLink = "";

    MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    MutableLiveData<Boolean> getSubmitEnabled() {
        return submitEnabled;
    }

    MutableLiveData<Boolean> getSubmitSuccess() {
        return submitSuccess;
    }

    void updateForm(String title, String link) {
        currentTitle = title != null ? title.trim() : "";
        currentLink = link != null ? link.trim() : "";
        submitEnabled.setValue(!TextUtils.isEmpty(currentTitle) && !TextUtils.isEmpty(currentLink));
    }

    void submit() {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }
        String title = currentTitle;
        String link = currentLink;
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(link)) {
            errorMessage.setValue(BaseApplication.getAppContext().getString(R.string.share_article_error_empty));
            return;
        }
        if (!isValidHttpLink(link)) {
            errorMessage.setValue(BaseApplication.getAppContext().getString(R.string.share_article_error_invalid_link));
            return;
        }

        loading.setValue(true);
        autoDispose(repository.shareArticle(title, link)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    loading.setValue(false);
                    if (result.isSuccess()) {
                        submitSuccess.setValue(true);
                    } else {
                        handleDomainError(result.getError());
                    }
                }, throwable -> {
                    loading.setValue(false);
                    handleThrowable(throwable);
                }));
    }

    private boolean isValidHttpLink(String link) {
        return link.startsWith("http://") || link.startsWith("https://");
    }

    private void handleDomainError(DomainError error) {
        if (error != null && !TextUtils.isEmpty(error.getMessage())) {
            errorMessage.setValue(error.getMessage());
        } else {
            errorMessage.setValue(BaseApplication.getAppContext().getString(R.string.share_article_error_submit));
        }
    }

    private void handleThrowable(Throwable throwable) {
        if (throwable != null && !TextUtils.isEmpty(throwable.getMessage())) {
            errorMessage.setValue(throwable.getMessage());
        } else {
            errorMessage.setValue(BaseApplication.getAppContext().getString(R.string.share_article_error_submit));
        }
    }
}
