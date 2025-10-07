package com.core.data.network;

import com.core.data.api.*;
import com.core.network.NetManager;

public final class NetApis {

    public static void init() {
        NetApis.Login();
        NetApis.Home();
        NetApis.User();
        NetApis.Content();
        NetApis.Message();
    }

    public static LoginApi Login() {
        return NetManager.createNetApi(LoginApi.class);
    }

    public static HomeApi Home() {
        return NetManager.createNetApi(HomeApi.class);
    }

    public static UserApi User() {
        return NetManager.createNetApi(UserApi.class);
    }

    public static ContentApi Content() {
        return NetManager.createNetApi(ContentApi.class);
    }

    public static MessageApi Message() {
        return NetManager.createNetApi(MessageApi.class);
    }
}
