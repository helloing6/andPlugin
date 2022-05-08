package com.android.mlplugin;

import android.app.Application;
import android.content.Context;

import com.android.mlplugin.broad.ReceiverHelper;
import com.android.mlplugin.service2.HookHelper;

import java.io.File;


public class App extends Application {

    private static Context mApp;

    public static Context getContext() {
        return mApp;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mApp = base;
    }

    @Override
    public void onCreate() {
        super.onCreate();



//        HookHelper.HookAMS();


    }
}
