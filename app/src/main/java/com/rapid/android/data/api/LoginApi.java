package com.rapid.android.data.api;

import com.core.net.base.BaseResponse;
import com.rapid.android.data.model.LoginBean;
import com.rapid.android.data.model.RegisterBean;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface LoginApi {

    // 注册
    @FormUrlEncoded
    @POST("/user/register")
    Observable<BaseResponse<RegisterBean>> register(
            @Field("username") String username,
            @Field("password") String password,
            @Field("repassword") String rePassword
    );

    // 登录
    @FormUrlEncoded
    @POST("/user/login")
    Observable<BaseResponse<LoginBean>> login(
            @Field("username") String username,
            @Field("password") String password
    );

    // 退出登录
    @GET("/user/logout/json")
    Observable<BaseResponse<String>> logout();
}
