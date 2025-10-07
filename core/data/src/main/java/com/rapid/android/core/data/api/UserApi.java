package com.rapid.android.core.data.api;

import com.rapid.android.core.domain.model.*;
import com.rapid.android.core.network.base.BaseResponse;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.*;

public interface UserApi {

    @GET("/user/lg/userinfo/json")
    Observable<BaseResponse<UserInfoBean>> userinfo();

    @GET("/lg/collect/list/{page}/json")
    Observable<BaseResponse<ArticleListBean>> collectList(@Path("page") int page);

    @FormUrlEncoded
    @POST("/lg/uncollect/{id}/json")
    Observable<BaseResponse<String>> unCollectInMine(
            @Path("id") int id,
            @Field("originId") int originId
    );

    @POST("/lg/uncollect_originId/{id}/json")
    Observable<BaseResponse<String>> unCollect(@Path("id") int id);

    @GET("/lg/coin/userinfo/json")
    Observable<BaseResponse<CoinBean>> signIn();

    @POST("/lg/collect/{id}/json")
    Observable<BaseResponse<String>> collect(@Path("id") int id);

    @GET("/lg/collect/usertools/json")
    Observable<BaseResponse<List<UserToolBean>>> userTools();

    @FormUrlEncoded
    @POST("/lg/collect/addtool/json")
    Observable<BaseResponse<UserToolBean>> addUserTool(
            @Field("name") String name,
            @Field("link") String link
    );

    @FormUrlEncoded
    @POST("/lg/collect/updatetool/json")
    Observable<BaseResponse<UserToolBean>> updateUserTool(
            @Field("id") int id,
            @Field("name") String name,
            @Field("link") String link
    );

    @FormUrlEncoded
    @POST("/lg/collect/deletetool/json")
    Observable<BaseResponse<String>> deleteUserTool(@Field("id") int id);

    @FormUrlEncoded
    @POST("/lg/collect/add/json")
    Observable<BaseResponse<ArticleListBean.Data>> collectOutside(
            @Field("title") String title,
            @Field("author") String author,
            @Field("link") String link
    );

    @FormUrlEncoded
    @POST("/lg/collect/user_article/update/{id}/json")
    Observable<BaseResponse<ArticleListBean.Data>> updateCollectedArticle(
            @Path("id") int articleId,
            @Field("title") String title,
            @Field("link") String link,
            @Field("author") String author
    );

    @GET("/user/lg/private_articles/{page}/json")
    Observable<BaseResponse<UserShareBean>> privateShareArticles(
            @Path("page") int page,
            @Query("page_size") Integer pageSize
    );

    @POST("/lg/user_article/delete/{id}/json")
    Observable<BaseResponse<String>> deleteShareArticle(@Path("id") int articleId);

    @FormUrlEncoded
    @POST("/lg/user_article/add/json")
    Observable<BaseResponse<ArticleListBean.Data>> addShareArticle(
            @Field("title") String title,
            @Field("link") String link
    );
}
