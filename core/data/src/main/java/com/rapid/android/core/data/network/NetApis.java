package com.rapid.android.core.data.network;

import com.rapid.android.core.data.api.*;
import com.rapid.android.core.network.client.NetworkClientManager;

public final class NetApis {

    public static LoginApi Login() {
        return NetworkClientManager.getDefaultClient().createService(LoginApi.class);
    }

    public static HomeApi Home() {
        return NetworkClientManager.getDefaultClient().createService(HomeApi.class);
    }

    public static UserApi User() {
        return NetworkClientManager.getDefaultClient().createService(UserApi.class);
    }

    public static ContentApi Content() {
        return NetworkClientManager.getDefaultClient().createService(ContentApi.class);
    }

    public static MessageApi Message() {
        return NetworkClientManager.getDefaultClient().createService(MessageApi.class);
    }
}
