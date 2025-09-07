package com.rapid.android.ui.fragment.home.fragments.recommend;

import androidx.lifecycle.MutableLiveData;

import com.rapid.android.api.NetApis;
import com.rapid.android.bean.ArticleListBean;
import com.rapid.android.bean.BannerItemBean;
import com.rapid.core.base.vm.BaseViewModel;
import com.rapid.core.net.base.BaseResponse;

import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class RecommendViewModel extends BaseViewModel {

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<BannerItemBean>> bannerList = new MutableLiveData<>();
    private final MutableLiveData<ArticleListBean> articleListBean = new MutableLiveData<>();

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
        autoDispose(NetApis.Main().banner()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<>() {

                    @Override
                    public void onNext(BaseResponse<ArrayList<BannerItemBean>> bannerRes) {
                        if (bannerRes.getData() != null) {
                            bannerList.setValue(bannerRes.getData());
                        } else {
                            errorMessage.setValue("登陆失败");
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
        autoDispose(NetApis.Main().articleList(0)
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
