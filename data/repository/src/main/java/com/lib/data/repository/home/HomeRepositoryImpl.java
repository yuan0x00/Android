package com.lib.data.repository.home;

import com.lib.data.api.HomeApi;
import com.lib.data.mapper.DomainResultMapper;
import com.lib.data.network.NetApis;
import com.lib.domain.model.ArticleListBean;
import com.lib.domain.model.BannerItemBean;
import com.lib.domain.repository.HomeRepository;
import com.lib.domain.result.DomainResult;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;

public class HomeRepositoryImpl implements HomeRepository {
    @Override
    public Observable<DomainResult<ArrayList<BannerItemBean>>> banner() {
        return api().banner()
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }

    @Override
    public Observable<DomainResult<ArticleListBean>> homeArticles(int page) {
        return api().articleList(page)
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }

    @Override
    public Observable<DomainResult<ArticleListBean>> projectArticles(int page) {
        return api().listProjectList(page)
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }

    @Override
    public Observable<DomainResult<ArticleListBean>> plazaArticles(int page) {
        return api().userArticleList(page)
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }

    private HomeApi api() {
        return NetApis.Home();
    }
}
