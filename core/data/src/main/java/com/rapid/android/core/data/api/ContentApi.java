package com.rapid.android.core.data.api;

import com.rapid.android.core.domain.model.*;
import com.rapid.android.core.network.base.BaseResponse;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.*;

public interface ContentApi {
    // 2. 体系
    @GET("/tree/json")
    Observable<BaseResponse<List<CategoryNodeBean>>> knowledgeTree();

    @GET("/article/list/{page}/json")
    Observable<BaseResponse<ArticleListBean>> articlesByCategory(@Path("page") int page, @Query("cid") int categoryId);

    @GET("/article/list/{page}/json")
    Observable<BaseResponse<ArticleListBean>> articlesByAuthor(@Path("page") int page, @Query("author") String author);

    @GET("/navi/json")
    Observable<BaseResponse<List<NavigationBean>>> navigation();

    // 项目
    @GET("/project/tree/json")
    Observable<BaseResponse<List<CategoryNodeBean>>> projectTree();

    @GET("/project/list/{page}/json")
    Observable<BaseResponse<ProjectPageBean>> projectArticles(@Path("page") int page, @Query("cid") int categoryId);

    // 15. 公众号 Tab
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

    // 11. 问答
    @GET("/wenda/list/{page}/json")
    Observable<BaseResponse<ArticleListBean>> questionAnswers(@Path("page") int page);

    @GET("/wenda/comments/{questionId}/json")
    Observable<BaseResponse<PageBean<WendaCommentBean>>> wendaComments(@Path("questionId") int questionId);

    // 10.2 分享人对应列表数据
    @GET("/user/{userId}/share_articles/{page}/json")
    Observable<BaseResponse<ArticleListBean>> userSharedArticles(@Path("userId") int userId, @Path("page") int page);

    // 热门/发现类
    @GET("/popular/wenda/json")
    Observable<BaseResponse<List<ArticleListBean.Data>>> popularWenda();

    @GET("/popular/column/json")
    Observable<BaseResponse<List<PopularColumnBean>>> popularColumns();

    @GET("/popular/route/json")
    Observable<BaseResponse<List<CategoryNodeBean>>> popularRoutes();

    // 9. 积分
    @GET("/lg/coin/list/{page}/json")
    Observable<BaseResponse<PageBean<CoinRecordBean>>> coinRecords(@Path("page") int page);

    @GET("/coin/rank/{page}/json")
    Observable<BaseResponse<PageBean<CoinRankBean>>> coinRank(@Path("page") int page);
}
