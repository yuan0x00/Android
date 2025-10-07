package com.rapid.android.core.data.local.dao;

import androidx.room.*;

import com.rapid.android.core.data.local.entity.UserEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    Single<UserEntity> getUserById(String id);

    @Query("SELECT * FROM users WHERE username = :username")
    Single<UserEntity> getUserByUsername(String username);

    @Query("SELECT * FROM users")
    Single<List<UserEntity>> getAllUsers();

    @Insert
    Completable insertUser(UserEntity user);

    @Update
    Completable updateUser(UserEntity user);

    @Delete
    Completable deleteUser(UserEntity user);

    @Query("DELETE FROM users")
    Completable deleteAllUsers();

    @Query("SELECT COUNT(*) FROM users")
    Single<Integer> getUsersCount();
}