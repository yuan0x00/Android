package com.lib.data.repository;

import com.lib.data.repository.content.ContentRepositoryImpl;
import com.lib.data.repository.home.HomeRepositoryImpl;
import com.lib.data.repository.user.UserRepositoryImpl;
import com.lib.domain.repository.ContentRepository;
import com.lib.domain.repository.HomeRepository;
import com.lib.domain.repository.UserRepository;

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
