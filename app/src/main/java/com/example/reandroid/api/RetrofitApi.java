package com.example.reandroid.api;

import com.example.core.network.BaseResponse;
import com.example.reandroid.bean.*;

import java.util.ArrayList;

import io.reactivex.Observable;
import retrofit2.http.*;

public interface RetrofitApi {

    // 登录
    @FormUrlEncoded
    @POST("/user/login")
    Observable<BaseResponse<LoginBean>> login(
            @Field("username") String username,
            @Field("password") String password
    );

    // 注册
    @FormUrlEncoded
    @POST("/user/register")
    Observable<BaseResponse<RegisterBean>> register(
            @Field("username") String username,
            @Field("password") String password,
            @Field("repassword") String rePassword
    );

    // 获取积分信息
    @GET("/lg/coin/userinfo/json")
    Observable<BaseResponse<CoinBean>> coin();

    // 获取用户信息
    @GET("/user/lg/userinfo/json")
    Observable<BaseResponse<UserInfoBean>> userinfo();

    // 退出登录
    @GET("/user/logout/json")
    Observable<BaseResponse<String>> logout();

    // 获取 Banner 列表
    @GET("/banner/json")
    Observable<BaseResponse<ArrayList<BannerItemBean>>> banner();

    // 获取文章列表
    @GET("/article/list/{page}/json")
    Observable<BaseResponse<ArticleListBean>> articleList(@Path("page") int page);

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

    // 获取用户发布的文章
    @GET("/user_article/list/{page}/json")
    Observable<BaseResponse<UserArticleBean>> userArticleList(@Path("page") int page);
}