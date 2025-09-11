package com.rapid.android.viewmodel;

import androidx.lifecycle.MutableLiveData;

import com.core.base.vm.BaseViewModel;
import com.core.net.base.BaseResponse;
import com.rapid.android.data.model.ArticleListBean;
import com.rapid.android.data.network.NetApis;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TopicViewModel extends BaseViewModel {
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<ArticleListBean> articleListBean = new MutableLiveData<>();

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<ArticleListBean> getArticleListBean() {
        return articleListBean;
    }

    public void articleList() {
        autoDispose(NetApis.Main().listProjectList(0)
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

                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }
}
