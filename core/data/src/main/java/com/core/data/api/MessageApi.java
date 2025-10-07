package com.core.data.api;

import com.core.domain.model.MessageBean;
import com.core.domain.model.PageBean;
import com.core.network.base.BaseResponse;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MessageApi {

    @GET("/message/lg/count_unread/json")
    Observable<BaseResponse<Integer>> unreadCount();

    @GET("/message/lg/readed_list/{page}/json")
    Observable<BaseResponse<PageBean<MessageBean>>> readMessages(
            @Path("page") int page,
            @Query("page_size") Integer pageSize
    );

    @GET("/message/lg/unread_list/{page}/json")
    Observable<BaseResponse<PageBean<MessageBean>>> unreadMessages(
            @Path("page") int page,
            @Query("page_size") Integer pageSize
    );

    @GET("/message/lg/push/list/{page}/json")
    Observable<BaseResponse<PageBean<MessageBean>>> pushedMessages(
            @Path("page") int page,
            @Query("page_size") Integer pageSize
    );
}
