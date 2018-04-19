package com.example.subayyal.dailynotes.Activities;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.example.subayyal.dailynotes.HelperClasses.AppConstants;
import com.example.subayyal.dailynotes.HelperClasses.Credentials;
import com.example.subayyal.dailynotes.LocalDatabase.AppDatabase;
import com.example.subayyal.dailynotes.LocalDatabase.AppDatabaseProvider;
import com.example.subayyal.dailynotes.MyBroadcastReciever;
import com.example.subayyal.dailynotes.R;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class WelcomeScreen extends AppCompatActivity {

    GoogleSignInAccount account;
    boolean isConnected;
    MyBroadcastReciever br;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        //initialize AWS Cognito and local database
        initializeCognito();

        //initialize intent
        Intent intent = new Intent(this, LoginScreen.class);

        if (checkFirstRun()) {
            Toast.makeText(this, "First Run!", Toast.LENGTH_LONG).show();
            startActivity(intent);
        } else {
            //this means its not first run but google account is null and have to be relogged in
            startActivity(new Intent(this, HomeScreen.class));
        }


    }

    private boolean checkFirstRun() {
       return getSharedPreferences(AppConstants.AppName, MODE_PRIVATE)
               .getBoolean("isFirstRun",true);
    }

    private void initializeCognito() {
        //initialize AWS Cognito
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(this,
                AppConstants.IdentityPoolId,
                Regions.US_EAST_1);
        Credentials.getInstance().setCredentialsProvider(credentialsProvider);
        Log.d("Test", "CREDENTIALS PROVIDER SESSION DURATION: " + credentialsProvider.getSessionDuration());
        //initialize local database
        AppDatabase appDatabase = Room
                .databaseBuilder(this, AppDatabase.class, AppConstants.DatabaseName)
                .build();
        AppDatabaseProvider.getInstance().setAppDatabase(appDatabase);
    }
}
