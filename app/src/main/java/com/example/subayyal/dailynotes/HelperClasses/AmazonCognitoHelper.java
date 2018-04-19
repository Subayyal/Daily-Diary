package com.example.subayyal.dailynotes.HelperClasses;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.example.subayyal.dailynotes.Activities.HomeScreen;
import com.example.subayyal.dailynotes.Activities.LoginScreen;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by subayyal on 3/15/2018.
 */

public class AmazonCognitoHelper {
    private CognitoCachingCredentialsProvider credentialsProvider;
    private Context context;
    private GoogleSignInAccount account;

    public AmazonCognitoHelper(Context context, GoogleSignInAccount account) {
        credentialsProvider = Credentials.getInstance().getCredentialsProvider();
        this.context = context;
        this.account = account;
        CognitoSignIn();
    }

    private void CognitoSignIn() {
        Map<String, String> logins = new HashMap<String, String>();
        logins.put(AppConstants.GoogleToken, account.getIdToken());
        credentialsProvider.setLogins(logins);
        Credentials.getInstance().setCredentialsProvider(credentialsProvider);


        Single<String> observable = Single.create(getCredentialId());

        SingleObserver<String> observer = new SingleObserver<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(String id) {
                if (id != null) {
                    if(context.getSharedPreferences(AppConstants.AppName, MODE_PRIVATE).getBoolean("isFirstRun", true)) {
                        Log.d("Test", "Cognito Id found. calling CloudSyncHelper");
                        new CloudSyncHelper();
                    }else {

                        //GOTO HOMESCREEN FROM HERE
                        Intent intent = new Intent(context, HomeScreen.class);
                        context.startActivity(intent);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(context,"Login Failed", Toast.LENGTH_SHORT).show();
                Log.d("Failed Login", "Login failed");
            }
        };

        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }

    private SingleOnSubscribe<String> getCredentialId() {
        return new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> emitter) throws Exception {
                String id = Credentials.getInstance().getCredentialsProvider().getIdentityId();
                emitter.onSuccess(id);
            }
        };
    }






}
