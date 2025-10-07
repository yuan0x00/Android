package com.core.data.api;

import com.core.domain.model.*;
import com.core.network.base.BaseResponse;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface HomeApi {

    // 1.2 首页 Banner
    @GET("/banner/json")
    Observable<BaseResponse<ArrayList<BannerItemBean>>> banner();

    // 1.1 首页文章列表
    @GET("/article/list/{page}/json")
    Observable<BaseResponse<ArticleListBean>> articleList(@Path("page") int page);

    // 16.1 最新项目 Tab（首页第二个 Tab）
    @GET("/article/listproject/{page}/json")
    Observable<BaseResponse<ArticleListBean>> listProjectList(@Path("page") int page);

    // 10.1 广场列表数据
    @GET("/user_article/list/{page}/json")
    Observable<BaseResponse<ArticleListBean>> userArticleList(
            @Path("page") int page,
            @Query("page_size") Integer pageSize
    );

    // 1.5 置顶文章
    @GET("/article/top/json")
    Observable<BaseResponse<List<ArticleListBean.Data>>> topArticles();

    // 1.3 常用网站
    @GET("/friend/json")
    Observable<BaseResponse<List<FriendLinkBean>>> friendLinks();

    // 1.4 搜索热词
    @GET("/hotkey/json")
    Observable<BaseResponse<List<HotKeyBean>>> hotKeys();

    // 鸿蒙专区入口
    @GET("/harmony/index/json")
    Observable<BaseResponse<HarmonyIndexBean>> harmonyIndex();

    // 17. 工具列表接口
    @GET("/tools/list/json")
    Observable<BaseResponse<List<ToolItemBean>>> toolList();
}
