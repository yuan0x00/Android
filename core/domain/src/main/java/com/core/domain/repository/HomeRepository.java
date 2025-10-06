package com.core.domain.repository;

import com.core.domain.model.ArticleListBean;
import com.core.domain.model.BannerItemBean;
import com.core.domain.result.DomainResult;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;

public interface HomeRepository {

    Observable<DomainResult<ArrayList<BannerItemBean>>> banner();

    Observable<DomainResult<ArticleListBean>> homeArticles(int page);

    Observable<DomainResult<ArticleListBean>> projectArticles(int page);

    Observable<DomainResult<ArticleListBean>> plazaArticles(int page);
}
