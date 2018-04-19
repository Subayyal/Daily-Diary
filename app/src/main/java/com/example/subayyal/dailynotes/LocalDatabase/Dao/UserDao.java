package com.example.subayyal.dailynotes.LocalDatabase.Dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.example.subayyal.dailynotes.LocalDatabase.Entities.UserEntity;

import java.util.List;

/**
 * Created by subayyal on 3/22/2018.
 */

@Dao
public interface UserDao {
    @Query("Select * FROM Users")
    List<UserEntity> getAll();

    @Query("Select * FROM Users WHERE user_id LIKE:user_id")
    UserEntity getUser(String user_id);

    @Insert
    void insertUser(UserEntity user);

    @Delete
    void deleteUser(UserEntity user);
}
