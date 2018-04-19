package com.example.subayyal.dailynotes.LocalDatabase.Dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.example.subayyal.dailynotes.LocalDatabase.Entities.MediaEntity;

import java.util.List;

/**
 * Created by subayyal on 3/22/2018.
 */

@Dao
public interface MediaDao {
    @Insert
    void insertMedia(MediaEntity media);

    @Delete
    void deleteMedia(MediaEntity media);

    @Query("SELECT * FROM Media WHERE note_id LIKE:note_id")
    List<MediaEntity> getAll(String note_id);
}
