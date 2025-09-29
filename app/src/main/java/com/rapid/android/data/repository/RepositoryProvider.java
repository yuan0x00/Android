package com.rapid.android.data.repository;

import com.rapid.android.data.repository.content.ContentRepository;
import com.rapid.android.data.repository.home.HomeRepository;

public final class RepositoryProvider {

    private static volatile HomeRepository homeRepository;
    private static volatile ContentRepository contentRepository;
    private static volatile UserRepositoryImpl userRepository;
    private static volatile UserRemoteDataSourceImpl userRemote;
    private static volatile UserLocalDataSourceImpl userLocal;

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

    public static UserRepositoryImpl getUserRepository() {
        if (userRepository == null) {
            synchronized (RepositoryProvider.class) {
                if (userRepository == null) {
                    userRepository = new UserRepositoryImpl(getUserRemote(), getUserLocal());
                }
            }
        }
        return userRepository;
    }

    private static UserRemoteDataSourceImpl getUserRemote() {
        if (userRemote == null) {
            synchronized (RepositoryProvider.class) {
                if (userRemote == null) {
                    userRemote = new UserRemoteDataSourceImpl();
                }
            }
        }
        return userRemote;
    }

    private static UserLocalDataSourceImpl getUserLocal() {
        if (userLocal == null) {
            synchronized (RepositoryProvider.class) {
                if (userLocal == null) {
                    userLocal = new UserLocalDataSourceImpl();
                }
            }
        }
        return userLocal;
    }
}
