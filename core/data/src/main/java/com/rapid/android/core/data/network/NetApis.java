package com.rapid.android.core.data.network;

import com.rapid.android.core.data.api.*;
import com.rapid.android.core.network.NetManager;

public final class NetApis {

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
