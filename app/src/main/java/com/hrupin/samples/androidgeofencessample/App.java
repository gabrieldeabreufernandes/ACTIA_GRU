package com.hrupin.samples.androidgeofencessample;

import android.app.Application;

/**
   Gabriel Fernandes 12/17.
 */
public class App extends Application {

    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static App getInstance() {
        return instance;
    }
}
