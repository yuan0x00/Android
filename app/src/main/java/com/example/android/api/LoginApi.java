package com.example.android.api;

import com.example.android.bean.LoginBean;
import com.example.android.bean.RegisterBean;
import com.example.core.net.base.BaseResponse;

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
