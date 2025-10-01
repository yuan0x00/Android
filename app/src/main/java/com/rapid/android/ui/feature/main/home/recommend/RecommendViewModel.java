package com.rapid.android.ui.feature.main.home.recommend;

import androidx.lifecycle.MutableLiveData;

import com.core.ui.presentation.BaseViewModel;
import com.lib.data.repository.RepositoryProvider;
import com.lib.domain.model.ArticleListBean;
import com.lib.domain.model.BannerItemBean;
import com.lib.domain.repository.HomeRepository;
import com.lib.domain.result.DomainError;
import com.lib.domain.result.DomainResult;

import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class RecommendViewModel extends BaseViewModel {

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<BannerItemBean>> bannerList = new MutableLiveData<>();
    private final MutableLiveData<ArticleListBean> articleListBean = new MutableLiveData<>();
    private final HomeRepository repository = RepositoryProvider.getHomeRepository();

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<ArrayList<BannerItemBean>> getBannerList() {
        return bannerList;
    }

    public MutableLiveData<ArticleListBean> getArticleListBean() {
        return articleListBean;
    }

    public void banner() {
        autoDispose(repository.banner()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<DomainResult<ArrayList<BannerItemBean>>>() {

                    @Override
                    public void onNext(DomainResult<ArrayList<BannerItemBean>> result) {
                        if (result.isSuccess() && result.getData() != null) {
                            bannerList.setValue(result.getData());
                        } else {
                            DomainError error = result.getError();
                            errorMessage.setValue(error != null ? error.getMessage() : "获取 Banner 失败");
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

    public void articleList() {
        autoDispose(repository.homeArticles(0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<DomainResult<ArticleListBean>>() {

                    @Override
                    public void onNext(DomainResult<ArticleListBean> result) {
                        if (result.isSuccess() && result.getData() != null) {
                            articleListBean.setValue(result.getData());
                        } else {
                            DomainError error = result.getError();
                            errorMessage.setValue(error != null ? error.getMessage() : "获取文章失败");
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
