package com.rapid.android.data.repository.home;

import com.rapid.android.data.api.HomeApi;
import com.rapid.android.data.mapper.DomainResultMapper;
import com.rapid.android.data.network.NetApis;
import com.rapid.android.domain.model.ArticleListBean;
import com.rapid.android.domain.model.BannerItemBean;
import com.rapid.android.domain.repository.HomeRepository;
import com.rapid.android.domain.result.DomainResult;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;

public class HomeRepositoryImpl implements HomeRepository {

    private final HomeApi api = NetApis.Home();

    @Override
    public Observable<DomainResult<ArrayList<BannerItemBean>>> banner() {
        return api.banner()
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }

    @Override
    public Observable<DomainResult<ArticleListBean>> homeArticles(int page) {
        return api.articleList(page)
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }

    @Override
    public Observable<DomainResult<ArticleListBean>> projectArticles(int page) {
        return api.listProjectList(page)
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }

    @Override
    public Observable<DomainResult<ArticleListBean>> plazaArticles(int page) {
        return api.userArticleList(page)
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }
}
