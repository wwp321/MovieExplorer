package com.byron.movieexplorer;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Build;

import timber.log.Timber;

public class MyApplication extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        Timber.plant(new Timber.DebugTree());
    }

    public static Context getContext() {
        return context;
    }
}
