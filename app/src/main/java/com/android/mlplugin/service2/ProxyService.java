package com.android.mlplugin.service2;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.android.mlplugin.BuildConfig;
import com.android.mlplugin.plugin.PluginApp;
import com.android.mlplugin.plugin.PluginManger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ProxyService extends Service {

    public static final String TARGET_SERVICE = "target_service";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (BuildConfig.DEBUG) {
            Log.d("ProxyService", "wanlihua debug work here? ProxyService");
        }

        if (intent == null || !intent.hasExtra(TARGET_SERVICE)){
            return START_STICKY;
        }
        String serviceName = intent.getStringExtra(TARGET_SERVICE);
        if (null == serviceName){
            return START_STICKY;
        }

        Service targetService = null;
        try {
            Class activityThreadClazz = Class.forName("android.app.ActivityThread");
            Method getActivityThreadMethod = activityThreadClazz.getDeclaredMethod("getApplicationThread");
            getActivityThreadMethod.setAccessible(true);

            Object activityThread = FieldUtils.getField(activityThreadClazz, null, "sCurrentActivityThread");
            Object applicationThread = getActivityThreadMethod.invoke(activityThread);

            Class iInterfaceClazz = Class.forName("android.os.IInterface");
            Method asBinderMethod = iInterfaceClazz.getDeclaredMethod("asBinder");
            asBinderMethod.setAccessible(true);
            Object token = asBinderMethod.invoke(applicationThread);
            Class serviceClazz = Class.forName("android.app.Service");
            Method attachMethod = serviceClazz.getDeclaredMethod("attach",Context.class, activityThreadClazz,
            String.class, IBinder.class, Application.class, Object.class);
            attachMethod.setAccessible(true);

            Object defaultSingleton = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O /*26*/){
                Class<?> activtiyManagerClass = Class.forName("android.app.ActivityManager");
                //获取activityManager中的IActivityManagerSingleton字段
                defaultSingleton = FieldUtils.getField(activtiyManagerClass,null,"IActivityManagerSingleton");
            }else {
                Class<?> activtiyManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
                defaultSingleton = FieldUtils.getField(activtiyManagerNativeClass,null,"gDefault");
            }

            Class<?> singletonClazz = Class.forName("android.util.Singleton");
            Field mInstanceField= FieldUtils.getField(singletonClazz ,"mInstance");//2
            //获取iActivityManager
            Object iActivityManager = mInstanceField.get(defaultSingleton);//3
            //Class<?> iActivityManagerClazz = Class.forName("android.app.IActivityManager");

            PluginApp pluginApp = PluginManger.getLoadedPluginApk();
            targetService = (Service) pluginApp.mClassLoader.loadClass(serviceName).newInstance();

//            targetService = (Service) Class.forName(serviceName).newInstance();
            attachMethod.invoke(targetService, this, activityThread,
                    intent.getComponent().getClassName(), token, getApplication(),iActivityManager);
            targetService.onCreate();
        }catch (Exception e){
            e.printStackTrace();
            Log.e("MALEI",e.toString());
        }
        targetService.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }
}
