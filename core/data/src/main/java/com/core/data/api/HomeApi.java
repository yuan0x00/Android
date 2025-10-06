package com.core.data.api;

import com.core.domain.model.ArticleListBean;
import com.core.domain.model.BannerItemBean;
import com.core.network.base.BaseResponse;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface HomeApi {

    @GET("/banner/json")
    Observable<BaseResponse<ArrayList<BannerItemBean>>> banner();

    @GET("/article/list/{page}/json")
    Observable<BaseResponse<ArticleListBean>> articleList(@Path("page") int page);

    @GET("/article/listproject/{page}/json")
    Observable<BaseResponse<ArticleListBean>> listProjectList(@Path("page") int page);

    @GET("/user_article/list/{page}/json")
    Observable<BaseResponse<ArticleListBean>> userArticleList(@Path("page") int page);
}
