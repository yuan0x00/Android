package com.lib.data.repository.content;

import com.core.network.base.BaseResponse;
import com.lib.data.api.ContentApi;
import com.lib.data.mapper.DomainResultMapper;
import com.lib.data.network.NetApis;
import com.lib.domain.model.*;
import com.lib.domain.repository.ContentRepository;
import com.lib.domain.result.DomainResult;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

public class ContentRepositoryImpl implements ContentRepository {

    private final ContentApi api = NetApis.Content();

    @Override
    public Observable<DomainResult<List<CategoryNodeBean>>> knowledgeTree() {
        return map(api.knowledgeTree());
    }

    @Override
    public Observable<DomainResult<ArticleListBean>> articlesByCategory(int page, int categoryId) {
        return map(api.articlesByCategory(page, categoryId));
    }

    @Override
    public Observable<DomainResult<List<NavigationBean>>> navigation() {
        return map(api.navigation());
    }

    @Override
    public Observable<DomainResult<List<CategoryNodeBean>>> projectTree() {
        return map(api.projectTree());
    }

    @Override
    public Observable<DomainResult<ArticleListBean>> projectArticles(int page, int categoryId) {
        return map(api.projectArticles(page, categoryId));
    }

    @Override
    public Observable<DomainResult<List<WxChapterBean>>> wechatChapters() {
        return map(api.weChatChapters());
    }

    @Override
    public Observable<DomainResult<ArticleListBean>> wechatArticles(int chapterId, int page) {
        return map(api.weChatArticles(chapterId, page));
    }

    @Override
    public Observable<DomainResult<ArticleListBean>> wechatArticlesSearch(int chapterId, int page, String keyword) {
        return map(api.weChatArticlesSearch(chapterId, page, keyword));
    }

    @Override
    public Observable<DomainResult<ArticleListBean>> searchArticles(int page, String keyword) {
        return map(api.searchArticles(page, keyword));
    }

    @Override
    public Observable<DomainResult<List<HotKeyBean>>> hotKeys() {
        return map(api.hotKeys());
    }

    @Override
    public Observable<DomainResult<List<FriendLinkBean>>> friendLinks() {
        return map(api.friendLinks());
    }

    @Override
    public Observable<DomainResult<ArticleListBean>> questionAnswers(int page) {
        return map(api.questionAnswers(page));
    }

    @Override
    public Observable<DomainResult<ArticleListBean>> userShares(int userId, int page) {
        return map(api.userSharedArticles(userId, page));
    }

    @Override
    public Observable<DomainResult<PageBean<CoinRecordBean>>> coinRecords(int page) {
        return map(api.coinRecords(page));
    }

    @Override
    public Observable<DomainResult<PageBean<CoinRankBean>>> coinRank(int page) {
        return map(api.coinRank(page));
    }

    private <T> Observable<DomainResult<T>> map(Observable<BaseResponse<T>> source) {
        return source
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }
}
