package com.rapid.android.ui.feature.main.home.lastproject;

import androidx.lifecycle.MutableLiveData;

import com.core.ui.presentation.BaseViewModel;
import com.rapid.android.data.repository.RepositoryProvider;
import com.rapid.android.domain.model.ArticleListBean;
import com.rapid.android.domain.repository.HomeRepository;
import com.rapid.android.domain.result.DomainError;
import com.rapid.android.domain.result.DomainResult;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LastProjectViewModel extends BaseViewModel {
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<ArticleListBean> articleListBean = new MutableLiveData<>();
    private final HomeRepository repository = RepositoryProvider.getHomeRepository();

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<ArticleListBean> getArticleListBean() {
        return articleListBean;
    }

    public void listProjectList() {
        autoDispose(repository.projectArticles(0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<DomainResult<ArticleListBean>>() {

                    @Override
                    public void onNext(DomainResult<ArticleListBean> result) {
                        if (result.isSuccess() && result.getData() != null) {
                            articleListBean.setValue(result.getData());
                        } else {
                            DomainError error = result.getError();
                            errorMessage.setValue(error != null ? error.getMessage() : "获取项目失败");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        errorMessage.setValue(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }
}
