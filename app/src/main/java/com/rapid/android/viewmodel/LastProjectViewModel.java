package com.rapid.android.viewmodel;

import androidx.lifecycle.MutableLiveData;

import com.core.network.base.BaseResponse;
import com.core.presentation.viewmodel.BaseViewModel;
import com.rapid.android.data.model.ArticleListBean;
import com.rapid.android.data.repository.RepositoryProvider;
import com.rapid.android.data.repository.home.HomeRepository;

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
                .subscribeWith(new DisposableObserver<>() {

                    @Override
                    public void onNext(BaseResponse<ArticleListBean> response) {
                        if (response.getData() != null) {
                            articleListBean.setValue(response.getData());
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
