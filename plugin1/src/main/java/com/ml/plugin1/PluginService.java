package com.ml.plugin1;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class PluginService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("MALEI","启动插件Service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("MALEI","启动插件onStartCommand");
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("MALEI","启动插件onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
