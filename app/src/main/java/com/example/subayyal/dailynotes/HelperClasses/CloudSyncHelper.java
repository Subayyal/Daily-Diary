package com.example.subayyal.dailynotes.HelperClasses;

import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.example.subayyal.clientsdk.DailyNotesAPIClient;
import com.example.subayyal.clientsdk.model.GetNotesResponseObject;
import com.example.subayyal.clientsdk.model.GetUserResponseObject;
import com.example.subayyal.clientsdk.model.SuccessfulPostRequest;
import com.example.subayyal.clientsdk.model.UserObject;
import com.example.subayyal.dailynotes.Exceptions.CheckUserException;
import com.example.subayyal.dailynotes.Exceptions.CreateUserLocalException;
import com.example.subayyal.dailynotes.Exceptions.CreateUserRemoteException;
import com.example.subayyal.dailynotes.Exceptions.FetchDataException;
import com.example.subayyal.dailynotes.Exceptions.SaveDataLocalException;
import com.example.subayyal.dailynotes.LocalDatabase.AppDatabaseProvider;
import com.example.subayyal.dailynotes.LocalDatabase.Entities.NoteEntity;
import com.example.subayyal.dailynotes.LocalDatabase.Entities.UserEntity;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

/**
 * Created by subayyal on 3/28/2018.
 */

public class CloudSyncHelper {
    private ApiClientFactory apiClientFactory;
    private DailyNotesAPIClient apiClient;

    public CloudSyncHelper() {
        Log.d("Test", "CloudSyncHelper Constructor Called");
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setConnectionTimeout(30000);
        clientConfiguration.setSocketTimeout(30000);
        this.apiClientFactory = new ApiClientFactory().clientConfiguration(clientConfiguration);
        this.apiClient = apiClientFactory.build(DailyNotesAPIClient.class);
    }


    public Single<String> checkUser(String id) {
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> emitter) throws CheckUserException {
                try {
                    GetUserResponseObject getUserResponseObject = apiClient.usersIdGet(id);
                    Log.d("Test", "checkUserCall: Status: " + getUserResponseObject.getStatus());
                    emitter.onSuccess(getUserResponseObject.getBody().getUserId());
                } catch (AmazonServiceException e) {
                    e.printStackTrace();
                    emitter.onError(new CheckUserException(Integer.toString(e.getStatusCode())));
                } catch (Exception e) {
                    e.printStackTrace();
                    emitter.onError(new CheckUserException(Integer.toString(AppConstants.ERROR)));
                }
            }
        });
    }

    public Single<GetNotesResponseObject> fetchData(String id) {
        return Single.create(new SingleOnSubscribe<GetNotesResponseObject>() {
            @Override
            public void subscribe(SingleEmitter<GetNotesResponseObject> emitter) throws FetchDataException {
                try {
                    Log.d("Test", "fetchData Called");
                    GetNotesResponseObject notes = apiClient.usersIdNotesGet(id);
                    emitter.onSuccess(notes);
                } catch (AmazonServiceException e) {
                    e.printStackTrace();
                    throw new FetchDataException(Integer.toString(e.getStatusCode()));
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new FetchDataException(Integer.toString(AppConstants.ERROR));
                }
            }
        });
    }

    public Single<UserEntity> createUserRemote(UserEntity user) {
        return Single.create(new SingleOnSubscribe<UserEntity>() {
            @Override
            public void subscribe(SingleEmitter<UserEntity> emitter) throws CreateUserRemoteException {
                try {
                    UserObject userObject = new UserObject();
                    userObject.setUserId(user.getUser_id());
                    userObject.setFname(user.getFname());
                    userObject.setLname(user.getLname());
                    userObject.setEmail(user.getEmail());
                    SuccessfulPostRequest post = apiClient.usersPost(userObject);
                    emitter.onSuccess(user);
                } catch (AmazonServiceException e) {
                    e.printStackTrace();
                    throw new CreateUserRemoteException(Integer.toString(e.getStatusCode()));
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new CreateUserRemoteException(Integer.toString(AppConstants.ERROR));
                }
            }
        });
    }

    public Single<Integer> saveFetchedData(GetNotesResponseObject getNotesResponseObject) {
        return Single.create(new SingleOnSubscribe<Integer>() {
            @Override
            public void subscribe(SingleEmitter<Integer> emitter) throws SaveDataLocalException {
                Log.d("Test", "saveFetchedData Called");
                try {
                    AppDatabaseProvider.getInstance().getAppDatabase().beginTransaction();
                    //save user first
                    for (int i = 0; i < getNotesResponseObject.getBody().getNotes().size(); i++) {
                        String json = new Gson().toJson(getNotesResponseObject.getBody().getNotes().get(i));
                        NoteEntity noteEntity = new Gson().fromJson(json, NoteEntity.class);
                        noteEntity.setSynced(true);
                        noteEntity.setEdited(false);
                        noteEntity.setDeleted(false);
                        try {
                            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(noteEntity.getNote_timestamp());
                            noteEntity.setNote_timestamp(new SimpleDateFormat("MM-dd-yyyy").format(date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                            throw new SaveDataLocalException(Integer.toString(AppConstants.LOCAL_PARSE_ERROR));
                        }
                        AppDatabaseProvider.getInstance().getAppDatabase().noteDao().insertNote(noteEntity);
                    }
                    AppDatabaseProvider.getInstance().getAppDatabase().setTransactionSuccessful();
                    AppDatabaseProvider.getInstance().getAppDatabase().endTransaction();
                    emitter.onSuccess(0);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new SaveDataLocalException(Integer.toString(AppConstants.LOCAL_DB_ERROR));
                }
            }
        });
    }

    public Single<String> createUserLocal(UserEntity user) {
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> emitter) throws CreateUserLocalException {
                try {
                    AppDatabaseProvider.getInstance().getAppDatabase().userDao().insertUser(user);
                    emitter.onSuccess(user.getUser_id());
                } catch (SQLiteConstraintException e) {
                    e.printStackTrace();
                    throw new CreateUserLocalException(Integer.toString(AppConstants.LOCAL_DB_DUPLICATE));
                }catch (Exception e){
                    e.printStackTrace();
                    throw new CreateUserLocalException(Integer.toString(AppConstants.LOCAL_DB_ERROR));
                }
            }
        });
    }


}
