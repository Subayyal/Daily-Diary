package com.example.subayyal.dailynotes.LocalDatabase;

/**
 * Created by subayyal on 3/22/2018.
 */

//Singleton class to hold appdatabase instance as per android documentation

public class AppDatabaseProvider {
    private AppDatabase appDatabase;
    private static final AppDatabaseProvider ourInstance = new AppDatabaseProvider();

    public static AppDatabaseProvider getInstance() {
        return ourInstance;
    }

    private AppDatabaseProvider() {
    }

    public void setAppDatabase( AppDatabase appDatabase) {
        this.appDatabase = appDatabase;
    }

    public AppDatabase getAppDatabase() {
        return this.appDatabase;
    }
}
