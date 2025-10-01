package com.rapid.android.domain.repository;

import com.rapid.android.domain.model.ArticleListBean;
import com.rapid.android.domain.model.BannerItemBean;
import com.rapid.android.domain.result.DomainResult;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;

public interface HomeRepository {

    Observable<DomainResult<ArrayList<BannerItemBean>>> banner();

    Observable<DomainResult<ArticleListBean>> homeArticles(int page);

    Observable<DomainResult<ArticleListBean>> projectArticles(int page);

    Observable<DomainResult<ArticleListBean>> plazaArticles(int page);
}
