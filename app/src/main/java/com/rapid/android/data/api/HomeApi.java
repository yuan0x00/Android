package com.rapid.android.data.api;

import com.core.net.base.BaseResponse;
import com.rapid.android.data.model.ArticleListBean;
import com.rapid.android.data.model.BannerItemBean;

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
