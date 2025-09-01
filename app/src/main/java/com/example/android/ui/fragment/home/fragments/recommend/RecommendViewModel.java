package com.example.android.ui.fragment.home.fragments.recommend;

import androidx.lifecycle.MutableLiveData;

import com.example.android.api.NetApis;
import com.example.android.bean.BannerItemBean;
import com.example.core.base.BaseResponse;
import com.example.core.base.BaseViewModel;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class RecommendViewModel extends BaseViewModel {

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<BannerItemBean>> bannerList = new MutableLiveData<>();

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<ArrayList<BannerItemBean>> getBannerList() {
        return bannerList;
    }

    public void banner() {
        Observable<BaseResponse<ArrayList<BannerItemBean>>> observable = NetApis.Main().banner();
        autoDispose(observable
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
}
