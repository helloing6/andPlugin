package com.android.mlplugin.activity;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class XMActivityHook {


    /**
     * 将ActivityThread中的的Instrumentation实例替换为自定义的Instrumentation
     */
    public static void hookInstrumentation(Context context){
        try{
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            // 获取 ActivityThread类对象中的 currentActivityThread() 方法
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            // 因为 public static ActivityThread currentActivityThread(){} 是静态的方法，所以可以直接传null,执行方法
            // 到这里我们就可以获取到 全局的 sCurrentActivityThread 对象实例了
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);

            // 获取ActivityThread类对象中的mInstrumentation
            Field instrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
            instrumentationField.setAccessible(true);
            // 获取全局ActivityThread对象的mInstrumentation对象
            Instrumentation sInstrumentation = (Instrumentation) instrumentationField.get(currentActivityThread);

            // 创建自定义Instrumentation 类
            HookedInstrumentation instrumentation = new HookedInstrumentation(sInstrumentation,context);
            // 将ActivityThread中的的Instrumentation实例替换为自定义的Instrumentation
            instrumentationField.set(currentActivityThread, instrumentation);

        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    /**
     * 把Activity 中的Instrumentation对象也换成我们自己的
     */
    public static void hookActivityInstrumentation(Activity activity) {
        try{
            // 获取 Activity类对象中的mInstrumentation属性
            Field sActivityInstrumentationField = Activity.class.getDeclaredField("mInstrumentation");
            sActivityInstrumentationField.setAccessible(true);
            // 获取当前Activity实例对象的mInstrumentation对象
            Instrumentation sActivityInstrumentation = (Instrumentation) sActivityInstrumentationField.get(activity);
            HookedInstrumentation instrumentation = new HookedInstrumentation(sActivityInstrumentation,activity.getApplicationContext());
            sActivityInstrumentationField.set(activity, instrumentation);

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }


}
