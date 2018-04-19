package com.example.subayyal.dailynotes.LocalDatabase.Entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by subayyal on 3/22/2018.
 */

@Entity(tableName = "Users")
public class UserEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "user_id")
    private String user_id;

    @ColumnInfo(name = "fname")
    private String fname;

    @ColumnInfo(name = "lname")
    private String lname;

    @ColumnInfo(name = "email")
    private String email;

    @Ignore
    private List<NoteEntity> notes;

    public UserEntity(@NonNull String user_id, String fname, String lname, String email, List<NoteEntity> notes) {
        this.user_id = user_id;
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.notes = notes;
    }

    public UserEntity(@NonNull String user_id, String fname, String lname, String email) {
        this.user_id = user_id;
        this.fname = fname;
        this.lname = lname;
        this.email = email;
    }

    public List<NoteEntity> getNotes() {
        return notes;
    }

    public void setNotes(List<NoteEntity> notes) {
        this.notes = notes;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
