package com.example.subayyal.dailynotes.HelperClasses;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;

/**
 * Created by subayyal on 3/15/2018.
 */

//Singleton Credentials class to hold instance of credentialsProvider.

public class Credentials {

    private CognitoCachingCredentialsProvider credentialsProvider;

    private static final Credentials ourInstance = new Credentials();

    public static Credentials getInstance() {
        return ourInstance;
    }

    private Credentials() {
    }

    public CognitoCachingCredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    public void setCredentialsProvider(CognitoCachingCredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }
}
