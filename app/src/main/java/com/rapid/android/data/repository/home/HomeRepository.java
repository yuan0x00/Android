package com.rapid.android.data.repository.home;

import com.core.net.base.BaseResponse;
import com.rapid.android.data.api.HomeApi;
import com.rapid.android.data.model.ArticleListBean;
import com.rapid.android.data.model.BannerItemBean;
import com.rapid.android.data.network.NetApis;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;

public class HomeRepository {

    private final HomeApi api = NetApis.Home();

    public Observable<BaseResponse<ArrayList<BannerItemBean>>> banner() {
        return api.banner();
    }

    public Observable<BaseResponse<ArticleListBean>> homeArticles(int page) {
        return api.articleList(page);
    }

    public Observable<BaseResponse<ArticleListBean>> projectArticles(int page) {
        return api.listProjectList(page);
    }

    public Observable<BaseResponse<ArticleListBean>> plazaArticles(int page) {
        return api.userArticleList(page);
    }
}
