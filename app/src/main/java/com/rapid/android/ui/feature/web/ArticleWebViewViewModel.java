package com.rapid.android.ui.feature.web;

import androidx.lifecycle.MutableLiveData;

import com.rapid.android.R;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.data.repository.RepositoryProvider;
import com.rapid.android.core.domain.repository.UserRepository;
import com.rapid.android.core.domain.result.DomainResult;
import com.rapid.android.core.ui.presentation.BaseViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ArticleWebViewViewModel extends BaseViewModel {

    private final UserRepository repository = RepositoryProvider.getUserRepository();

    private final MutableLiveData<Boolean> collectState = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    private int articleId = -1;

    void init(int articleId, boolean collected) {
        this.articleId = articleId;
        collectState.setValue(collected);
    }

    MutableLiveData<Boolean> getCollectState() {
        return collectState;
    }

    MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    MutableLiveData<String> getToastMessage() {
        return toastMessage;
    }

    void toggleCollect() {
        if (articleId <= 0 || Boolean.TRUE.equals(loading.getValue())) {
            return;
        }
        boolean targetCollect = !Boolean.TRUE.equals(collectState.getValue());
        loading.setValue(true);
        if (targetCollect) {
            autoDispose(repository.collectArticle(articleId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleCollectResult, this::handleError));
        } else {
            autoDispose(repository.unCollectArticle(articleId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleUnCollectResult, this::handleError));
        }
    }

    private void handleCollectResult(DomainResult<String> result) {
        loading.setValue(false);
        if (result != null && result.isSuccess()) {
            collectState.setValue(true);
            toastMessage.setValue(BaseApplication.getAppContext().getString(R.string.article_collect_success));
        } else {
            toastMessage.setValue(BaseApplication.getAppContext().getString(R.string.article_collect_failed));
        }
    }

    private void handleUnCollectResult(DomainResult<String> result) {
        loading.setValue(false);
        if (result != null && result.isSuccess()) {
            collectState.setValue(false);
            toastMessage.setValue(BaseApplication.getAppContext().getString(R.string.article_uncollect_success));
        } else {
            toastMessage.setValue(BaseApplication.getAppContext().getString(R.string.article_collect_failed));
        }
    }

    private void handleError(Throwable throwable) {
        loading.setValue(false);
        toastMessage.setValue(BaseApplication.getAppContext().getString(R.string.article_collect_failed));
    }
}
