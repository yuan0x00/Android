package com.rapid.android.domain.repository;

import com.rapid.android.domain.model.*;
import com.rapid.android.domain.result.DomainResult;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

public interface ContentRepository {

    Observable<DomainResult<List<CategoryNodeBean>>> knowledgeTree();

    Observable<DomainResult<ArticleListBean>> articlesByCategory(int page, int categoryId);

    Observable<DomainResult<List<NavigationBean>>> navigation();

    Observable<DomainResult<List<CategoryNodeBean>>> projectTree();

    Observable<DomainResult<ArticleListBean>> projectArticles(int page, int categoryId);

    Observable<DomainResult<List<WxChapterBean>>> wechatChapters();

    Observable<DomainResult<ArticleListBean>> wechatArticles(int chapterId, int page);

    Observable<DomainResult<ArticleListBean>> wechatArticlesSearch(int chapterId, int page, String keyword);

    Observable<DomainResult<ArticleListBean>> searchArticles(int page, String keyword);

    Observable<DomainResult<List<HotKeyBean>>> hotKeys();

    Observable<DomainResult<List<FriendLinkBean>>> friendLinks();

    Observable<DomainResult<ArticleListBean>> questionAnswers(int page);

    Observable<DomainResult<ArticleListBean>> userShares(int userId, int page);

    Observable<DomainResult<PageBean<CoinRecordBean>>> coinRecords(int page);

    Observable<DomainResult<PageBean<CoinRankBean>>> coinRank(int page);
}
