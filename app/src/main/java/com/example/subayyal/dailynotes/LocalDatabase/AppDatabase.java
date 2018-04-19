package com.example.subayyal.dailynotes.LocalDatabase;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.example.subayyal.dailynotes.LocalDatabase.Dao.MediaDao;
import com.example.subayyal.dailynotes.LocalDatabase.Dao.NoteDao;
import com.example.subayyal.dailynotes.LocalDatabase.Dao.UserDao;
import com.example.subayyal.dailynotes.LocalDatabase.Entities.MediaEntity;
import com.example.subayyal.dailynotes.LocalDatabase.Entities.NoteEntity;
import com.example.subayyal.dailynotes.LocalDatabase.Entities.UserEntity;

/**
 * Created by subayyal on 3/22/2018.
 */

@Database(entities = {UserEntity.class,NoteEntity.class, MediaEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase{
    public abstract NoteDao noteDao();
    public abstract UserDao userDao();
    public abstract MediaDao mediaDao();
}
