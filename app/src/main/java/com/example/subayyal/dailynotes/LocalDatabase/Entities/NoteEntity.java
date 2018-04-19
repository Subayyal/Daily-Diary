package com.example.subayyal.dailynotes.LocalDatabase.Entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by subayyal on 3/22/2018.
 */

@Entity(tableName = "Notes", foreignKeys =
@ForeignKey(entity = UserEntity.class, parentColumns = "user_id", childColumns = "user_id", onDelete = ForeignKey.CASCADE))
public class NoteEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "note_id")
    private String note_id;

    @NonNull
    @ColumnInfo(name = "user_id")
    private String user_id;

    @ColumnInfo(name = "note_text")
    private String note_text;

    @ColumnInfo(name = "note_timestamp")
    private String note_timestamp;

    @ColumnInfo(name = "synced")
    private boolean synced;

    @ColumnInfo(name = "edited")
    private boolean edited;

    @ColumnInfo(name = "deleted")
    private boolean deleted;


    @Ignore
    private List<MediaEntity> media;

    public NoteEntity(@NonNull String note_id, @NonNull String user_id, String note_text, String note_timestamp, List<MediaEntity> media) {
        this.note_id = note_id;
        this.user_id = user_id;
        this.note_text = note_text;
        this.note_timestamp = note_timestamp;
        this.media = media;
    }

    public NoteEntity(@NonNull String note_id, @NonNull String user_id, String note_text, String note_timestamp, boolean synced, boolean edited, boolean deleted) {
        this.note_id = note_id;
        this.user_id = user_id;
        this.note_text = note_text;
        this.note_timestamp = note_timestamp;
        this.synced = synced;
        this.edited = edited;
        this.deleted = deleted;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public List<MediaEntity> getMedia() {
        return media;
    }

    public void setMedia(List<MediaEntity> media) {
        this.media = media;
    }

    public String getNote_id() {
        return note_id;
    }

    public void setNote_id(String note_id) {
        this.note_id = note_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getNote_text() {
        return note_text;
    }

    public void setNote_text(String note_text) {
        this.note_text = note_text;
    }

    public String getNote_timestamp() {
        return note_timestamp;
    }

    public void setNote_timestamp(String note_timestamp) {
        this.note_timestamp = note_timestamp;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

}
