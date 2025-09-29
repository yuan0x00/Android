package com.rapid.android.data.repository.content;

import com.core.net.base.BaseResponse;
import com.rapid.android.data.api.ContentApi;
import com.rapid.android.data.model.*;
import com.rapid.android.data.network.NetApis;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

public class ContentRepository {

    private final ContentApi api = NetApis.Content();

    public Observable<BaseResponse<List<CategoryNodeBean>>> knowledgeTree() {
        return api.knowledgeTree();
    }

    public Observable<BaseResponse<ArticleListBean>> articlesByCategory(int page, int categoryId) {
        return api.articlesByCategory(page, categoryId);
    }

    public Observable<BaseResponse<List<NavigationBean>>> navigation() {
        return api.navigation();
    }

    public Observable<BaseResponse<List<CategoryNodeBean>>> projectTree() {
        return api.projectTree();
    }

    public Observable<BaseResponse<ArticleListBean>> projectArticles(int page, int categoryId) {
        return api.projectArticles(page, categoryId);
    }

    public Observable<BaseResponse<List<WxChapterBean>>> wechatChapters() {
        return api.weChatChapters();
    }

    public Observable<BaseResponse<ArticleListBean>> wechatArticles(int chapterId, int page) {
        return api.weChatArticles(chapterId, page);
    }

    public Observable<BaseResponse<ArticleListBean>> wechatArticlesSearch(int chapterId, int page, String keyword) {
        return api.weChatArticlesSearch(chapterId, page, keyword);
    }

    public Observable<BaseResponse<ArticleListBean>> searchArticles(int page, String keyword) {
        return api.searchArticles(page, keyword);
    }

    public Observable<BaseResponse<List<HotKeyBean>>> hotKeys() {
        return api.hotKeys();
    }

    public Observable<BaseResponse<List<FriendLinkBean>>> friendLinks() {
        return api.friendLinks();
    }

    public Observable<BaseResponse<ArticleListBean>> questionAnswers(int page) {
        return api.questionAnswers(page);
    }

    public Observable<BaseResponse<ArticleListBean>> userShares(int userId, int page) {
        return api.userSharedArticles(userId, page);
    }

    public Observable<BaseResponse<PageBean<CoinRecordBean>>> coinRecords(int page) {
        return api.coinRecords(page);
    }

    public Observable<BaseResponse<PageBean<CoinRankBean>>> coinRank(int page) {
        return api.coinRank(page);
    }
}
