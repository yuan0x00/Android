package com.example.reandroid.net;

import com.example.reandroid.bean.ArticleListBean;
import com.example.reandroid.bean.BannerItemBean;
import com.example.reandroid.bean.BaseResponse;
import com.example.reandroid.bean.CoinBean;
import com.example.reandroid.bean.CollectBean;
import com.example.reandroid.bean.LoginBean;
import com.example.reandroid.bean.RegisterBean;
import com.example.reandroid.bean.UserArticleBean;
import com.example.reandroid.bean.UserInfoBean;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    // 登录
    @FormUrlEncoded
    @POST("/user/login")
    Call<BaseResponse<LoginBean>> login(
            @Field("username") String username,
            @Field("password") String password
    );

    // 注册
    @FormUrlEncoded
    @POST("/user/register")
    Call<BaseResponse<RegisterBean>> register(
            @Field("username") String username,
            @Field("password") String password,
            @Field("repassword") String rePassword
    );

    // 获取积分信息
    @GET("/lg/coin/userinfo/json")
    Call<BaseResponse<CoinBean>> coin();

    // 获取用户信息
    @GET("/user/lg/userinfo/json")
    Call<BaseResponse<UserInfoBean>> userinfo();

    // 退出登录
    @GET("/user/logout/json")
    Call<BaseResponse<String>> logout();

    // 获取 Banner 列表
    @GET("/banner/json")
    Call<BaseResponse<ArrayList<BannerItemBean>>> banner();

    // 获取文章列表
    @GET("/article/list/{page}/json")
    Call<BaseResponse<ArticleListBean>> articleList(@Path("page") int page);

    // 获取收藏列表
    @GET("/lg/collect/list/{page}/json")
    Call<BaseResponse<CollectBean>> collectList(@Path("page") int page);

    // 取消收藏（在收藏页中取消）
    @FormUrlEncoded
    @POST("/lg/uncollect/{id}/json")
    Call<BaseResponse<String>> unCollectInMine(
            @Path("id") int id,
            @Field("originId") int originId
    );

    // 取消收藏（在文章页取消）
    @POST("/lg/uncollect_originId/{id}/json")
    Call<BaseResponse<String>> unCollect(@Path("id") int id);

    // 收藏文章
    @POST("/lg/collect/{id}/json")
    Call<BaseResponse<String>> collect(@Path("id") int id);

    // 获取用户发布的文章
    @GET("/user_article/list/{page}/json")
    Call<BaseResponse<UserArticleBean>> userArticleList(@Path("page") int page);
}