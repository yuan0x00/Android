package com.rapid.android.data.session;

public final class SessionInitializer {

    private SessionInitializer() {
    }

    public static void restore() {
        SessionStateRepository.getInstance().restore();
    }
}
