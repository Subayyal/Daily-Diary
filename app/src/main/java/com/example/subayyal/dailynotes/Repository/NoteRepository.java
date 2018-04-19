package com.example.subayyal.dailynotes.Repository;

import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.example.subayyal.clientsdk.DailyNotesAPIClient;
import com.example.subayyal.clientsdk.model.NotesDeleteObject;
import com.example.subayyal.clientsdk.model.NotesDeleteObjectNotesItem;
import com.example.subayyal.clientsdk.model.NotesListObject;
import com.example.subayyal.clientsdk.model.NotesListObjectNotesItem;
import com.example.subayyal.dailynotes.LocalDatabase.AppDatabaseProvider;
import com.example.subayyal.dailynotes.LocalDatabase.Dao.NoteDao;
import com.example.subayyal.dailynotes.LocalDatabase.Entities.NoteEntity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

/**
 * Created by subayyal on 3/27/2018.
 */

public class NoteRepository {

    private ClientConfiguration clientConfiguration;
    private ApiClientFactory apiClientFactory;
    private DailyNotesAPIClient dailyNotesAPIClient;
    private Observable<Integer> saveCurrentNoteObservable;

    public NoteRepository() {
        clientConfiguration = new ClientConfiguration();
        clientConfiguration.setSocketTimeout(30000);
        apiClientFactory = new ApiClientFactory().clientConfiguration(clientConfiguration);
        dailyNotesAPIClient = apiClientFactory.build(DailyNotesAPIClient.class);
    }


    public LiveData<NoteEntity> getCurrentNote(String date) {
        return AppDatabaseProvider.getInstance().getAppDatabase().noteDao().getNoteByDate(date);
    }

    public LiveData<List<NoteDao.NoteDates>> getDates() {
        return AppDatabaseProvider.getInstance().getAppDatabase().noteDao().getDates();
    }

    public Single<Integer> deleteCurrentNoteLocal(NoteEntity noteEntity) {
        return Single.create(emitter -> {
            try {
                if (noteEntity.isSynced()) {
                    AppDatabaseProvider.getInstance().getAppDatabase()
                            .noteDao().markNoteDeleted(noteEntity.getNote_id());
                } else {
                    AppDatabaseProvider.getInstance().getAppDatabase()
                            .noteDao().deleteNote(noteEntity.getNote_id());
                }
                emitter.onSuccess(0);
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception();
            }
        });
    }

    public Single<Integer> saveCurrentNoteLocal(NoteEntity noteEntity) {
        return Single.create(new SingleOnSubscribe<Integer>() {
            @Override
            public void subscribe(SingleEmitter<Integer> emitter) throws Exception {
                Log.d("Test", "saveCurrentNoteLocal Called");
                try {
                    AppDatabaseProvider.getInstance().getAppDatabase().noteDao().insertNote(noteEntity);
                    emitter.onSuccess(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Single<Integer> saveCurrentEditedNoteLocal(NoteEntity noteEntity) {
        return Single.create(emitter -> {
            try {
                AppDatabaseProvider.getInstance().getAppDatabase().noteDao().editNote(noteEntity.getNote_id(), noteEntity.getNote_text());
            emitter.onSuccess(0);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
        });
    }

    public Single<List<NoteEntity>> cloudSyncForUnDeletedNotes() {
        return Single.create(emitter -> {
            Log.d("Test", "cloudSyncForUnDeletedNotes Called");
            List<NoteEntity> noteEntities = AppDatabaseProvider.getInstance().getAppDatabase().noteDao().getAllUnDeletedNotesToSync();
            if (noteEntities == null || noteEntities.isEmpty()) {
                Log.d("Test", "cloudSyncForUnDeletedNotes No Notes Found");
                emitter.onSuccess(noteEntities);
                return;
            }
            NotesListObject notesListObject = new NotesListObject();
            notesListObject.setNotes(entityToNoteListObject(noteEntities));
            try {
                dailyNotesAPIClient.usersIdNotesPost(noteEntities.get(0).getUser_id(), notesListObject);

            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception();
            }
            emitter.onSuccess(noteEntities);
        });
    }

    public Single<Integer> updateFlagsForUnDeletedNotes(List<NoteEntity> noteEntities) {
        return Single.create(emitter -> {
            Log.d("Test", "updateFlagsForUnDeletedNotes Called");
            if (noteEntities == null || noteEntities.isEmpty()) {
                emitter.onSuccess(0);
                return;
            }
            for (int i = 0; i < noteEntities.size(); i++) {
                try {
                    AppDatabaseProvider.getInstance().getAppDatabase().noteDao().updateFlagForUnDeletedNotes(noteEntities.get(i).getNote_id());
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new Exception();
                }
                emitter.onSuccess(0);
            }
        });
    }

    public Single<List<NoteEntity>> cloudSyncForDeletedNotes() {
        return Single.create(emitter -> {
            Log.d("Test", "cloudSyncForDeletedNotes Called");
            List<NoteEntity> noteEntities = AppDatabaseProvider.getInstance().getAppDatabase().noteDao().getAllDeletedNotesToSync();
            if (noteEntities == null || noteEntities.isEmpty()) {
                Log.d("Test", "cloudSyncForDeletedNotes No Notes Found");
                emitter.onSuccess(noteEntities);
                return;
            }
            NotesDeleteObject notesDeleteObject = new NotesDeleteObject();
            notesDeleteObject.setNotes(entityToNoteDeleteObject(noteEntities));
            NotesListObject notesListObject = new NotesListObject();
            notesListObject.setNotes(entityToNoteListObject(noteEntities));
            try {
                dailyNotesAPIClient.notesDelete(notesDeleteObject);
                emitter.onSuccess(noteEntities);
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception();
            }
        });
    }

    public Single<Integer> updateDeletedNotesLocal(List<NoteEntity> noteEntities) {
        return Single.create(emitter -> {
            Log.d("Test", "updateDeletedNotesLocal Called");
            if (noteEntities == null || noteEntities.isEmpty()) {
                emitter.onSuccess(0);
                return;
            }
            for (int i = 0; i < noteEntities.size(); i++) {
                try {
                    AppDatabaseProvider.getInstance().getAppDatabase().noteDao().deleteNote(noteEntities.get(i).getNote_id());
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new Exception();
                }
                emitter.onSuccess(0);
            }
        });
    }

    public List<NotesListObjectNotesItem> entityToNoteListObject(List<NoteEntity> entities) {
        List<NotesListObjectNotesItem> list = new ArrayList<>();
        for (int i = 0; i < entities.size(); i++) {
            NotesListObjectNotesItem item = new NotesListObjectNotesItem();
            item.setNoteId(entities.get(i).getNote_id());
            item.setNoteText(entities.get(i).getNote_text());
            item.setUserId(entities.get(i).getUser_id());
            item.setNoteTimestamp(entities.get(i).getNote_timestamp());
            list.add(item);
        }
        return list;
    }

    public List<NotesDeleteObjectNotesItem> entityToNoteDeleteObject(List<NoteEntity> entities) {
        List<NotesDeleteObjectNotesItem> list = new ArrayList<>();
        for (int i = 0; i < entities.size(); i++) {
            NotesDeleteObjectNotesItem item = new NotesDeleteObjectNotesItem();
            item.setNoteId(entities.get(i).getNote_id());
            list.add(item);
        }
        return list;
    }


}
