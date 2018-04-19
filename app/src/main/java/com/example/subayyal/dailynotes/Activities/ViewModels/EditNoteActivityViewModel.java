package com.example.subayyal.dailynotes.Activities.ViewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.example.subayyal.dailynotes.HelperClasses.Utils;
import com.example.subayyal.dailynotes.LocalDatabase.Entities.NoteEntity;
import com.example.subayyal.dailynotes.Repository.NoteRepository;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by subayyal on 3/27/2018.
 */

public class EditNoteActivityViewModel extends AndroidViewModel {

    private NoteRepository noteRepository;
    private MutableLiveData<Boolean> shouldClose;

    public EditNoteActivityViewModel(@NonNull Application application) {
        super(application);
        this.noteRepository = new NoteRepository();
        this.shouldClose = new MutableLiveData<>();
        this.shouldClose.setValue(false);
    }

    public LiveData<Boolean> getShouldClose() {
        return shouldClose;
    }

    public void setShouldClose(Boolean shouldClose) {
        this.shouldClose.setValue(shouldClose);
    }

    public void saveNote(NoteEntity noteEntity) {
        noteRepository.saveCurrentNoteLocal(noteEntity)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        Utils.createToast(getApplication(), "Note saved" );
                        setShouldClose(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.createToast(getApplication(), "Unable to save." );
                    }
                });
    }

    public void saveEditNote(NoteEntity noteEntity) {
        noteRepository.saveCurrentEditedNoteLocal(noteEntity)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {}

                    @Override
                    public void onSuccess(Integer integer) {
                        Utils.createToast(getApplication(), "Note saved" );
                        setShouldClose(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.createToast(getApplication(), "Unable to save." );
                    }
                });
    }

}
