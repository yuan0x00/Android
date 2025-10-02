package com.lib.domain.repository;

import com.lib.domain.model.ArticleListBean;
import com.lib.domain.model.BannerItemBean;
import com.lib.domain.result.DomainResult;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;

public interface HomeRepository {

    Observable<DomainResult<ArrayList<BannerItemBean>>> banner();

    Observable<DomainResult<ArticleListBean>> homeArticles(int page);

    Observable<DomainResult<ArticleListBean>> projectArticles(int page);

    Observable<DomainResult<ArticleListBean>> plazaArticles(int page);
}
