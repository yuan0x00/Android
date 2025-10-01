package com.rapid.android.data.repository;

import com.rapid.android.data.repository.content.ContentRepositoryImpl;
import com.rapid.android.data.repository.home.HomeRepositoryImpl;
import com.rapid.android.data.repository.user.UserRepositoryImpl;
import com.rapid.android.domain.repository.ContentRepository;
import com.rapid.android.domain.repository.HomeRepository;
import com.rapid.android.domain.repository.UserRepository;

public final class RepositoryProvider {

    private static volatile HomeRepository homeRepository;
    private static volatile ContentRepository contentRepository;
    private static volatile UserRepository userRepository;

    private RepositoryProvider() {
    }

    public static HomeRepository getHomeRepository() {
        if (homeRepository == null) {
            synchronized (RepositoryProvider.class) {
                if (homeRepository == null) {
                    homeRepository = new HomeRepositoryImpl();
                }
            }
        }
        return homeRepository;
    }

    public static ContentRepository getContentRepository() {
        if (contentRepository == null) {
            synchronized (RepositoryProvider.class) {
                if (contentRepository == null) {
                    contentRepository = new ContentRepositoryImpl();
                }
            }
        }
        return contentRepository;
    }

    public static UserRepository getUserRepository() {
        if (userRepository == null) {
            synchronized (RepositoryProvider.class) {
                if (userRepository == null) {
                    userRepository = new UserRepositoryImpl();
                }
            }
        }
        return userRepository;
    }
}
