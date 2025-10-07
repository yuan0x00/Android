package com.core.data.repository.home;

import com.core.data.api.HomeApi;
import com.core.data.mapper.DomainResultMapper;
import com.core.data.network.NetApis;
import com.core.domain.model.*;
import com.core.domain.repository.HomeRepository;
import com.core.domain.result.DomainResult;

import java.util.ArrayList;
import java.util.List;

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
        return api().userArticleList(page, null)
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }

    @Override
    public Observable<DomainResult<List<ArticleListBean.Data>>> topArticles() {
        return api().topArticles()
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }

    @Override
    public Observable<DomainResult<List<FriendLinkBean>>> friendLinks() {
        return api().friendLinks()
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }

    @Override
    public Observable<DomainResult<List<HotKeyBean>>> hotKeys() {
        return api().hotKeys()
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }

    @Override
    public Observable<DomainResult<HarmonyIndexBean>> harmonyIndex() {
        return api().harmonyIndex()
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }

    @Override
    public Observable<DomainResult<List<ToolItemBean>>> toolList() {
        return api().toolList()
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }

    private HomeApi api() {
        return NetApis.Home();
    }
}
