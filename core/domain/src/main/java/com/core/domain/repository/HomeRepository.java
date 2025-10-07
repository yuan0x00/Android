package com.core.domain.repository;

import com.core.domain.model.*;
import com.core.domain.result.DomainResult;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;

public interface HomeRepository {

    Observable<DomainResult<ArrayList<BannerItemBean>>> banner();

    Observable<DomainResult<ArticleListBean>> homeArticles(int page);

    Observable<DomainResult<ArticleListBean>> projectArticles(int page);

    Observable<DomainResult<ArticleListBean>> plazaArticles(int page);

    Observable<DomainResult<List<ArticleListBean.Data>>> topArticles();

    Observable<DomainResult<List<FriendLinkBean>>> friendLinks();

    Observable<DomainResult<List<HotKeyBean>>> hotKeys();

    Observable<DomainResult<HarmonyIndexBean>> harmonyIndex();

    Observable<DomainResult<List<ToolItemBean>>> toolList();
}
