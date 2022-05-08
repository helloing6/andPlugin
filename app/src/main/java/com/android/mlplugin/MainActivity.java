package com.android.mlplugin;


import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.android.mlplugin.broad.ReceiverHelper;
import com.android.mlplugin.plugin.PluginManger;
import com.android.mlplugin.activity.XMActivityHook;
import com.android.mlplugin.service.HookServiceManager;

import java.io.File;

public class MainActivity extends Activity {

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        checkSelfPermission();

        // 插件加载
        PluginManger.loadPluginApk(this,"plugin1.apk");

        //activity的插件化方案
        XMActivityHook.hookInstrumentation(this);
        XMActivityHook.hookActivityInstrumentation(this);

        //service的插件化方案
        HookServiceManager.init();

        // 静态广播的插件化方案
        // 将插件中静态广播都扫描出来
        File testPlugin = getFileStreamPath("plugin1.apk");
        ReceiverHelper.preLoadReceiver(this, testPlugin);

        this.findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName("com.ml.plugin1",
                        "com.ml.plugin1.MainActivity");
                startActivity(intent);
            }
        });

        //启动服务
        this.findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ComponentName componentName = new ComponentName("com.ml.plugin1", "com.ml.plugin1.PluginService");
                intent = new Intent().setComponent(componentName);
                startService(intent);
            }
        });

        //关闭服务
        this.findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(intent);
            }
        });

        this.findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent("malei"));
            }
        });


    }

    //读写权限
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;
//    private void checkSelfPermission() {
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
//            }
//        }
//    }

}