package com.android.mlplugin.service2;

import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

public class HookHelper {

    public static final String TARGET_INTENT = "target_intent";

    public static void HookAMS ()  {
        try {
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
            Class<?> iActivityManagerClazz = Class.forName("android.app.IActivityManager");
            Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class<?>[] { iActivityManagerClazz }, new IActivityManagerProxy(iActivityManager));
            mInstanceField.set(defaultSingleton, proxy);
        }catch (Exception e){
            e.printStackTrace();
            Log.e("MALEI",e.toString());
        }

    }

}
