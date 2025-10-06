package com.core.data.local.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.core.common.app.BaseApplication;
import com.core.data.local.dao.UserDao;
import com.core.data.local.entity.UserEntity;

@Database(
    entities = {UserEntity.class},
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "rapid_android_db";
    private static volatile AppDatabase instance;

    public static AppDatabase getInstance() {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        BaseApplication.getAppContext(),
                        AppDatabase.class,
                        DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration() // 开发阶段使用，生产环境应使用迁移
                    .build();
                }
            }
        }
        return instance;
    }

    public abstract UserDao userDao();
}
