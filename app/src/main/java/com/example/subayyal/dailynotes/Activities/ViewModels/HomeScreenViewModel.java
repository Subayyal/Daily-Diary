package com.example.subayyal.dailynotes.Activities.ViewModels;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.example.subayyal.dailynotes.HelperClasses.Utils;
import com.example.subayyal.dailynotes.LocalDatabase.Dao.NoteDao;
import com.example.subayyal.dailynotes.LocalDatabase.Entities.NoteEntity;
import com.example.subayyal.dailynotes.MyBroadcastReciever;
import com.example.subayyal.dailynotes.NetworkStatus;
import com.example.subayyal.dailynotes.Repository.NoteRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by subayyal on 3/27/2018.
 */

public class HomeScreenViewModel extends AndroidViewModel {

    private NoteRepository noteRepository;
    private final MutableLiveData<String> currentDate = new MutableLiveData<>();
    private final LiveData<NoteEntity> currentNote = Transformations.switchMap(currentDate, new Function<String, LiveData<NoteEntity>>() {
        @Override
        public LiveData<NoteEntity> apply(String input) {
            return noteRepository.getCurrentNote(input);
        }
    });
    private MutableLiveData<Boolean> connected;
    private final IntentFilter intentFilter = new IntentFilter();
    private final MyBroadcastReciever br = new MyBroadcastReciever(new NetworkStatus() {
        @Override
        public void onDisconnect() {
            setConnected(false);
        }

        @Override
        public void onConnect() {
            setConnected(true);
        }
    });

    public HomeScreenViewModel(@NonNull Application application) {
        super(application);
        this.noteRepository = new NoteRepository();
        this.currentDate.setValue(new SimpleDateFormat("MM-dd-yyyy").format(new Date()));
        this.connected = new MutableLiveData<>();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getApplication().registerReceiver(br, intentFilter);
    }


    public MutableLiveData<Boolean> isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected.setValue(connected);
    }

    public LiveData<String> getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate.setValue(currentDate);
    }

    public LiveData<List<NoteDao.NoteDates>> getDates() {
        return noteRepository.getDates();
    }

    public LiveData<NoteEntity> getCurrentNote() {
        return currentNote;
    }

    public void cloudSync() {
        if(isConnected().getValue()) {
            Log.d("Test", "cloudSync Called");
            noteRepository.cloudSyncForUnDeletedNotes()
                    .flatMap(noteEntities -> {
                        return noteRepository.updateFlagsForUnDeletedNotes(noteEntities);
                    })
                    .flatMap(integer -> {
                        return noteRepository.cloudSyncForDeletedNotes();
                    })
                    .flatMap(noteEntities -> {
                        return noteRepository.updateDeletedNotesLocal(noteEntities);
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                        }

                        @Override
                        public void onSuccess(Integer entities) {
                            Utils.createToast(getApplication(), "Sync complete");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Utils.createToast(getApplication(), "Sync failed");
                        }
                    });
        }else{
            Utils.createToast(getApplication(), "No Network Connection");
        }
    }

    public void deleteCurrentNote() {
        noteRepository.deleteCurrentNoteLocal(getCurrentNote().getValue())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {}
                    @Override
                    public void onSuccess(Integer integer) {
                        Utils.createToast(getApplication(), "Note Deleted");
                    }
                    @Override
                    public void onError(Throwable e) {
                        Utils.createToast(getApplication(), "Unable to delete note");
                    }
                });
    }


}
