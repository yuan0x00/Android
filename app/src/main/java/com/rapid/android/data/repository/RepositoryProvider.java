package com.rapid.android.data.repository;

import com.rapid.android.data.repository.content.ContentRepository;
import com.rapid.android.data.repository.home.HomeRepository;
import com.rapid.android.data.repository.user.UserRepository;

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
                    homeRepository = new HomeRepository();
                }
            }
        }
        return homeRepository;
    }

    public static ContentRepository getContentRepository() {
        if (contentRepository == null) {
            synchronized (RepositoryProvider.class) {
                if (contentRepository == null) {
                    contentRepository = new ContentRepository();
                }
            }
        }
        return contentRepository;
    }

    public static UserRepository getUserRepository() {
        if (userRepository == null) {
            synchronized (RepositoryProvider.class) {
                if (userRepository == null) {
                    userRepository = new UserRepository();
                }
            }
        }
        return userRepository;
    }
}
