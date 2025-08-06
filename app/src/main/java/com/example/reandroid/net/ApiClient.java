package com.example.reandroid.net;

import retrofit2.Retrofit;

public class ApiClient {

    private static volatile ApiService apiService = null;

    public static ApiService getApiService() {
        if (apiService == null) {
            synchronized (ApiClient.class) {
                if (apiService == null) {
                    Retrofit retrofit = RetrofitBuilder.getInstance().getRetrofit();
                    apiService = retrofit.create(ApiService.class);
                }
            }
        }
        return apiService;
    }
}