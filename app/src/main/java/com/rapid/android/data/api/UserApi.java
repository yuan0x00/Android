package com.rapid.android.data.api;

import com.core.network.base.BaseResponse;
import com.rapid.android.data.model.ArticleListBean;
import com.rapid.android.data.model.CoinBean;
import com.rapid.android.data.model.UserInfoBean;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.*;

public interface UserApi {

    @GET("/lg/coin/userinfo/json")
    Observable<BaseResponse<CoinBean>> coin();

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

    @POST("/lg/collect/{id}/json")
    Observable<BaseResponse<String>> collect(@Path("id") int id);
}
