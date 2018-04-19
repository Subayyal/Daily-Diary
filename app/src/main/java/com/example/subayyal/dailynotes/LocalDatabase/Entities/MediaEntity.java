package com.example.subayyal.dailynotes.LocalDatabase.Entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by subayyal on 3/22/2018.
 */

@Entity(tableName = "Media", foreignKeys =
@ForeignKey(entity = NoteEntity.class, parentColumns = "note_id", childColumns = "note_id", onDelete = 5))
public class MediaEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "media_id")
    private String media_id;

    @NonNull
    @ColumnInfo(name = "note_id")
    private String note_id;

    @ColumnInfo(name = "media_url")
    private String media_url;

    @ColumnInfo(name = "media_timestamp")
    private String media_timestamp;


    public MediaEntity(@NonNull String media_id, @NonNull String note_id, String media_url, String media_timestamp) {
        this.media_id = media_id;
        this.note_id = note_id;
        this.media_url = media_url;
        this.media_timestamp = media_timestamp;
    }

    public String getMedia_id() {
        return media_id;
    }

    public void setMedia_id(String media_id) {
        this.media_id = media_id;
    }

    public String getNote_id() {
        return note_id;
    }

    public void setNote_id(String note_id) {
        this.note_id = note_id;
    }

    public String getMedia_url() {
        return media_url;
    }

    public void setMedia_url(String media_url) {
        this.media_url = media_url;
    }

    public String getMedia_timestamp() {
        return media_timestamp;
    }

    public void setMedia_timestamp(String media_timestamp) {
        this.media_timestamp = media_timestamp;
    }
}
