package com.example.subayyal.dailynotes.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.apigateway.ApiClientException;
import com.example.subayyal.dailynotes.Exceptions.CheckUserException;
import com.example.subayyal.dailynotes.Exceptions.CreateUserLocalException;
import com.example.subayyal.dailynotes.Exceptions.FetchDataException;
import com.example.subayyal.dailynotes.Exceptions.SaveDataLocalException;
import com.example.subayyal.dailynotes.HelperClasses.AmazonCognitoHelper;
import com.example.subayyal.dailynotes.HelperClasses.AppConstants;
import com.example.subayyal.dailynotes.HelperClasses.CloudSyncHelper;
import com.example.subayyal.dailynotes.HelperClasses.Credentials;
import com.example.subayyal.dailynotes.HelperClasses.Utils;
import com.example.subayyal.dailynotes.LocalDatabase.Entities.UserEntity;
import com.example.subayyal.dailynotes.MyBroadcastReciever;
import com.example.subayyal.dailynotes.NetworkStatus;
import com.example.subayyal.dailynotes.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class LoginScreen extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private ProgressBar progressBar;
    private SignInButton googleSignInButton;
    private CloudSyncHelper cloudSyncHelper;
    private boolean isConnected;
    private final IntentFilter intentFilter = new IntentFilter();
    private final MyBroadcastReciever br = new MyBroadcastReciever(new NetworkStatus() {
        @Override
        public void onDisconnect() {
            isConnected = false;
        }

        @Override
        public void onConnect() {
            isConnected = true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getApplication().registerReceiver(br, intentFilter);

        credentialsProvider = Credentials.getInstance().getCredentialsProvider();
        cloudSyncHelper = new CloudSyncHelper();
        progressBar = findViewById(R.id.indeterminateBar);
        googleSignInButton = findViewById(R.id.sign_in_button);

        //setting up google sign in client
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(AppConstants.GoogleClientId)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInButton.setOnClickListener(view -> {
            if (isConnected) {
                googleSignInButton.setEnabled(false);
                googleSignIn();
            } else {
                Utils.createToast(this, "No Network Found");
            }
        });
    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, AppConstants.GoogleRequestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        // Response returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == AppConstants.GoogleRequestCode) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

        if(resultCode == 0){
            googleSignInButton.setEnabled(true);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            progressBar.setVisibility(View.VISIBLE);
            cognitoSignIn(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("test", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    public void cognitoSignIn(GoogleSignInAccount account) {
        Map<String, String> logins = new HashMap<String, String>();
        logins.put(AppConstants.GoogleToken, account.getIdToken());
        credentialsProvider.setLogins(logins);
        Credentials.getInstance().setCredentialsProvider(credentialsProvider);

        getCredentialsId()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onSuccess(String id) {
                        if (id != null) {
                            if (getSharedPreferences(AppConstants.AppName, MODE_PRIVATE).getBoolean("isFirstRun", true)) {
                                Log.d("Test", "Cognito Id found. calling CloudSyncHelper");
                                firstRunSync(account);
                            } else {
                                //GOTO HOMESCREEN FROM HERE
                                googleSignInButton.setEnabled(true);
                                progressBar.setVisibility(View.GONE);
                                Intent intent = new Intent(LoginScreen.this, HomeScreen.class);
                                startActivity(intent);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        googleSignInButton.setEnabled(true);
                        progressBar.setVisibility(View.INVISIBLE);
                        Utils.createToast(LoginScreen.this, "Login Failed");
                    }
                });
    }

    public Single<String> getCredentialsId() {
        return Single.create(emitter -> {
            try {
                String id = Credentials.getInstance().getCredentialsProvider().getIdentityId();
                emitter.onSuccess(id);
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception();
            }
        });
    }

    public void firstRunSync(GoogleSignInAccount account) {
        UserEntity user = new UserEntity(account.getId(), account.getGivenName(), account.getFamilyName(), account.getEmail());


        cloudSyncHelper.checkUser(user.getUser_id())
                .retry(3, new Predicate<Throwable>() {
                    @Override
                    public boolean test(Throwable throwable) throws Exception {
                        Log.d("Test", throwable.toString());
                        if (throwable instanceof SocketTimeoutException) {
                            Log.d("Test", "Time out.. Retrying..");
                            return true;
                        }
                        return false;
                    }
                })
                .flatMap(s -> {
                    return cloudSyncHelper.createUserLocal(user)
                            .onErrorResumeNext(throwable -> {
                                Log.d("Test", "onErrorResumeNext, throwable message: " + throwable.getMessage());
                                if (throwable instanceof CreateUserLocalException) {
                                    if (Integer.parseInt(throwable.getMessage()) == AppConstants.LOCAL_DB_DUPLICATE) {
                                        return Single.just(user.getUser_id());
                                    }
                                }
                                return Single.error(new CreateUserLocalException(Integer.toString(AppConstants.LOCAL_DB_ERROR)));
                            });
                })
                .flatMap(id -> {
                    return cloudSyncHelper.fetchData(id)
                            .retry(3, new Predicate<Throwable>() {
                                @Override
                                public boolean test(Throwable throwable) throws Exception {
                                    Log.d("Test", throwable.toString());
                                    if (throwable instanceof SocketTimeoutException) {
                                        Log.d("Test", "Time out.. Retrying..");
                                        return true;
                                    }
                                    return false;
                                }
                            });
                })
                .flatMap(notesResponseObject -> {
                    return cloudSyncHelper.saveFetchedData(notesResponseObject);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        //handle onsuccess here
                        googleSignInButton.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                        Log.d("Test", "onSuccess Called");
                        getSharedPreferences(AppConstants.AppName, MODE_PRIVATE).edit().putBoolean("isFirstRun", false).apply();
                        startActivity(new Intent(LoginScreen.this, HomeScreen.class));
                    }

                    @Override
                    public void onError(Throwable e) {

                        if (e instanceof SocketTimeoutException) {
                            googleSignInButton.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                            Log.d("Test", "Socket Time Out");
                            Utils.createToast(LoginScreen.this, "Socket timed out");
                            return;
                        }

                        int code = Integer.parseInt(e.getMessage());
                        Log.d("Test", "onError Called");
                        if (e instanceof CheckUserException) {
                            Log.d("Test", "onError CheckUserException");
                            if (code == AppConstants.NOTFOUND) {
                                newUserSequence(user);
                            } else {
                                googleSignInButton.setEnabled(true);
                                progressBar.setVisibility(View.GONE);
                                Utils.createToast(LoginScreen.this, "Unable to user information from cloud. Try again.");
                            }
                        }
                        if (e instanceof CreateUserLocalException) {
                            Log.d("Test", "onError CreateUserLocalException");
                            googleSignInButton.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                        }
                        if (e instanceof FetchDataException) {
                            Log.d("Test", "onError FetchDataException");
                            if (code == AppConstants.NOTFOUND) {
                                googleSignInButton.setEnabled(true);
                                progressBar.setVisibility(View.GONE);
                                getSharedPreferences(AppConstants.AppName, MODE_PRIVATE).edit().putBoolean("isFirstRun", false).apply();
                                startActivity(new Intent(LoginScreen.this, HomeScreen.class));
                            } else {
                                googleSignInButton.setEnabled(true);
                                progressBar.setVisibility(View.GONE);
                                Log.d("Test", "Unable to fetch data from cloud");
                                Utils.createToast(LoginScreen.this, "Unable to fetch data from cloud. Try again.");
                            }
                        }
                        if (e instanceof SaveDataLocalException) {
                            googleSignInButton.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                            Log.d("Test", "onError SaveDataLocalException");
                            if (code == AppConstants.LOCAL_DB_ERROR) {
                                Log.d("Test", "Unable to save data fetched from cloud");
                                Utils.createToast(LoginScreen.this, "Unable to save data fetched from cloud");
                            } else {
                                Utils.createToast(LoginScreen.this, "Unable to save data fetched from cloud");
                            }
                        }
                    }
                });

    }

    private void newUserSequence(UserEntity user) {
        cloudSyncHelper.createUserRemote(user)
                .flatMap(userEntity -> {
                    return cloudSyncHelper.createUserLocal(userEntity);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onSuccess(String String) {
                        googleSignInButton.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                        getSharedPreferences(AppConstants.AppName, MODE_PRIVATE).edit().putBoolean("isFirstRun", false).apply();
                        startActivity(new Intent(LoginScreen.this, HomeScreen.class));
                    }

                    @Override
                    public void onError(Throwable e) {
                        googleSignInButton.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                        Log.d("Test", "newUserSequence onError CheckUserException");
                        Log.d("Test", "Unable to create user profile. Try again.");
                        Utils.createToast(LoginScreen.this, "Unable to create user profile. Try again.");
                    }
                });
    }


}
