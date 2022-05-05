package com.android.mlplugin;

import android.app.Application;

import com.android.mlplugin.broad.ReceiverHelper;
import com.android.mlplugin.service2.HookHelper;

import java.io.File;


public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        HookHelper.HookAMS();


    }
}
