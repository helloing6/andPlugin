package com.android.mlplugin.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class StubService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("MALEI","宿主的预服务 启动");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
