package com.example.subayyal.dailynotes.LocalDatabase.Dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.subayyal.dailynotes.LocalDatabase.Entities.NoteEntity;

import java.util.List;

/**
 * Created by subayyal on 3/22/2018.
 */
// false = 0
// true = 1
@Dao
public interface NoteDao {
    @Query("SELECT * FROM Notes WHERE (synced = 0 AND deleted = 0) OR (synced = 1 AND edited = 1 AND deleted = 0)")
    List<NoteEntity> getAllUnDeletedNotesToSync();

    @Query("SELECT * FROM Notes WHERE synced = 1 AND deleted = 1")
    List<NoteEntity> getAllDeletedNotesToSync();

    @Query("UPDATE Notes SET synced = 1, edited = 0 WHERE note_id = :note_id")
    void updateFlagForUnDeletedNotes(String note_id);

    @Query("SELECT * FROM Notes WHERE note_timestamp LIKE :date AND deleted = 0")
    LiveData<NoteEntity> getNoteByDate(String date);


    @Query("SELECT note_id, note_timestamp FROM Notes WHERE deleted = 0 ORDER BY note_timestamp DESC")
    LiveData<List<NoteDates>> getDates();

    @Query("UPDATE Notes SET deleted = 1  WHERE note_id LIKE :note_id")
    void markNoteDeleted(String note_id);

    @Query("UPDATE Notes Set note_text = :text, edited = 1 WHERE note_id = :note_id")
    void editNote(String note_id, String text);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(NoteEntity note);

    @Query("DELETE FROM Notes Where note_id = :note_id")
    void deleteNote(String note_id);

    static class NoteDates {
        String note_id;
        String note_timestamp;

        public String getNote_timestamp() {
            return note_timestamp;
        }

        public void setNote_timestamp(String note_timestamp) {
            this.note_timestamp = note_timestamp;
        }

        public String getNote_id() {
            return note_id;
        }

        public void setNote_id(String note_id) {
            this.note_id = note_id;
        }
    }


}
