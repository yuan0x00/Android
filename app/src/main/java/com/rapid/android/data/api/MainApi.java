package com.rapid.android.data.api;

import com.core.net.base.BaseResponse;
import com.rapid.android.data.model.*;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.*;

public interface MainApi {

    // 获取积分信息
    @GET("/lg/coin/userinfo/json")
    Observable<BaseResponse<CoinBean>> coin();

    // 获取用户信息
    @GET("/user/lg/userinfo/json")
    Observable<BaseResponse<UserInfoBean>> userinfo();

    // 获取 Banner 列表
    @GET("/banner/json")
    Observable<BaseResponse<ArrayList<BannerItemBean>>> banner();

    // 获取首页tab-1-文章列表
    @GET("/article/list/{page}/json")
    Observable<BaseResponse<ArticleListBean>> articleList(@Path("page") int page);

    // 获取首页tab-2-最新项目列表
    @GET("/article/listproject/{page}/json")
    Observable<BaseResponse<ArticleListBean>> listProjectList(@Path("page") int page);

    // 获取首页tab-3-近期分享列表
    @GET("/user_article/list/{page}/json")
    Observable<BaseResponse<ArticleListBean>> userArticleList(@Path("page") int page);


    // 获取收藏列表
    @GET("/lg/collect/list/{page}/json")
    Observable<BaseResponse<CollectBean>> collectList(@Path("page") int page);

    // 取消收藏（在收藏页中取消）
    @FormUrlEncoded
    @POST("/lg/uncollect/{id}/json")
    Observable<BaseResponse<String>> unCollectInMine(
            @Path("id") int id,
            @Field("originId") int originId
    );

    // 取消收藏（在文章页取消）
    @POST("/lg/uncollect_originId/{id}/json")
    Observable<BaseResponse<String>> unCollect(@Path("id") int id);

    // 收藏文章
    @POST("/lg/collect/{id}/json")
    Observable<BaseResponse<String>> collect(@Path("id") int id);

}