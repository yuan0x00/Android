package com.rapid.android.data.api;

import com.core.network.base.BaseResponse;
import com.rapid.android.domain.model.*;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.*;

public interface ContentApi {

    // 首页拓展
    @GET("/article/top/json")
    Observable<BaseResponse<List<ArticleListBean.Data>>> topArticles();

    // 知识体系
    @GET("/tree/json")
    Observable<BaseResponse<List<CategoryNodeBean>>> knowledgeTree();

    @GET("/article/list/{page}/json")
    Observable<BaseResponse<ArticleListBean>> articlesByCategory(@Path("page") int page, @Query("cid") int categoryId);

    @GET("/navi/json")
    Observable<BaseResponse<List<NavigationBean>>> navigation();

    // 项目
    @GET("/project/tree/json")
    Observable<BaseResponse<List<CategoryNodeBean>>> projectTree();

    @GET("/project/list/{page}/json")
    Observable<BaseResponse<ArticleListBean>> projectArticles(@Path("page") int page, @Query("cid") int categoryId);

    // 公众号
    @GET("/wxarticle/chapters/json")
    Observable<BaseResponse<List<WxChapterBean>>> weChatChapters();

    @GET("/wxarticle/list/{id}/{page}/json")
    Observable<BaseResponse<ArticleListBean>> weChatArticles(@Path("id") int chapterId, @Path("page") int page);

    @GET("/wxarticle/list/{id}/{page}/json")
    Observable<BaseResponse<ArticleListBean>> weChatArticlesSearch(@Path("id") int chapterId, @Path("page") int page, @Query("k") String keyword);

    // 搜索
    @FormUrlEncoded
    @POST("/article/query/{page}/json")
    Observable<BaseResponse<ArticleListBean>> searchArticles(@Path("page") int page, @Field("k") String keyword);

    @GET("/hotkey/json")
    Observable<BaseResponse<List<HotKeyBean>>> hotKeys();

    @GET("/friend/json")
    Observable<BaseResponse<List<FriendLinkBean>>> friendLinks();

    // 问答广场
    @GET("/wenda/list/{page}/json")
    Observable<BaseResponse<ArticleListBean>> questionAnswers(@Path("page") int page);

    @GET("/user/{userId}/share_articles/{page}/json")
    Observable<BaseResponse<ArticleListBean>> userSharedArticles(@Path("userId") int userId, @Path("page") int page);

    // 积分
    @GET("/lg/coin/list/{page}/json")
    Observable<BaseResponse<PageBean<CoinRecordBean>>> coinRecords(@Path("page") int page);

    @GET("/coin/rank/{page}/json")
    Observable<BaseResponse<PageBean<CoinRankBean>>> coinRank(@Path("page") int page);
}
