package com.rapid.android.core.data.repository.content;

import com.rapid.android.core.data.api.ContentApi;
import com.rapid.android.core.data.api.HomeApi;
import com.rapid.android.core.data.mapper.DomainResultMapper;
import com.rapid.android.core.data.network.NetApis;
import com.rapid.android.core.domain.model.*;
import com.rapid.android.core.domain.repository.ContentRepository;
import com.rapid.android.core.domain.result.DomainResult;
import com.rapid.android.core.network.base.BaseResponse;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

public class ContentRepositoryImpl implements ContentRepository {
    @Override
    public Observable<DomainResult<List<CategoryNodeBean>>> knowledgeTree() {
        return map(api().knowledgeTree());
    }

    @Override
    public Observable<DomainResult<ArticleListBean>> articlesByCategory(int page, int categoryId) {
        return map(api().articlesByCategory(page, categoryId));
    }

    @Override
    public Observable<DomainResult<List<NavigationBean>>> navigation() {
        return map(api().navigation());
    }

    @Override
    public Observable<DomainResult<List<CategoryNodeBean>>> projectTree() {
        return map(api().projectTree());
    }

    @Override
    public Observable<DomainResult<ProjectPageBean>> projectArticles(int page, int categoryId) {
        return api().projectArticles(page, categoryId)
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }

    @Override
    public Observable<DomainResult<List<WxChapterBean>>> wechatChapters() {
        return map(api().weChatChapters());
    }

    @Override
    public Observable<DomainResult<ArticleListBean>> wechatArticles(int chapterId, int page) {
        return map(api().weChatArticles(chapterId, page));
    }

    @Override
    public Observable<DomainResult<ArticleListBean>> wechatArticlesSearch(int chapterId, int page, String keyword) {
        return map(api().weChatArticlesSearch(chapterId, page, keyword));
    }

    @Override
    public Observable<DomainResult<ArticleListBean>> searchArticles(int page, String keyword) {
        return map(api().searchArticles(page, keyword));
    }

    @Override
    public Observable<DomainResult<List<HotKeyBean>>> hotKeys() {
        return homeApi().hotKeys()
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }

    @Override
    public Observable<DomainResult<List<FriendLinkBean>>> friendLinks() {
        return homeApi().friendLinks()
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }

    @Override
    public Observable<DomainResult<List<ArticleListBean.Data>>> popularWenda() {
        return map(api().popularWenda());
    }

    @Override
    public Observable<DomainResult<List<PopularColumnBean>>> popularColumns() {
        return map(api().popularColumns());
    }

    @Override
    public Observable<DomainResult<List<CategoryNodeBean>>> popularRoutes() {
        return map(api().popularRoutes());
    }

    @Override
    public Observable<DomainResult<List<CategoryNodeBean>>> tutorialChapters() {
        return map(api().tutorialChapters());
    }

    @Override
    public Observable<DomainResult<ArticleListBean>> tutorialArticles(int page, int tutorialId) {
        return map(api().tutorialArticles(page, tutorialId, 1));
    }

    @Override
    public Observable<DomainResult<ArticleListBean>> questionAnswers(int page) {
        return map(api().questionAnswers(page));
    }

    @Override
    public Observable<DomainResult<ArticleListBean>> userShares(int userId, int page) {
        return map(api().userSharedArticles(userId, page));
    }

    @Override
    public Observable<DomainResult<PageBean<CoinRecordBean>>> coinRecords(int page) {
        return map(api().coinRecords(page));
    }

    @Override
    public Observable<DomainResult<PageBean<CoinRankBean>>> coinRank(int page) {
        return map(api().coinRank(page));
    }

    private <T> Observable<DomainResult<T>> map(Observable<BaseResponse<T>> source) {
        return source
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }

    private ContentApi api() {
        return NetApis.Content();
    }

    private HomeApi homeApi() {
        return NetApis.Home();
    }
}
