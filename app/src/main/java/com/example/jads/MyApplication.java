package com.example.jads;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Firebase disk persistence for offline caching
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
