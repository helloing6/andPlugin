package com.android.mlplugin.activity;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextThemeWrapper;

import com.android.mlplugin.plugin.PluginApp;
import com.android.mlplugin.plugin.PluginManger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class HookedInstrumentation extends Instrumentation  {

    private final Instrumentation mBase;
    private final Context mContext;

    public final static String KEY_IS_PLUGIN = "key_is_plugin";
    public final static String KEY_PACKAGE =  "key_package";
    public final static String KEY_ACTIVITY = "key_activity";

    public HookedInstrumentation(Instrumentation base, Context context) {
        mBase = base;
        mContext = context;
    }

    /**
     * 覆盖掉原始Instrumentation类的对应方法,用于插件内部跳转Activity时适配
     *
     * @Override
     */
    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {

        // hookToStubActivity
        if (intent == null || intent.getComponent() == null) {
            return null;
        }
        String targetPackageName = intent.getComponent().getPackageName();
        String targetClassName = intent.getComponent().getClassName();


        if (mContext != null
//                && !mContext.getPackageName().equals(targetPackageName)
                && isPluginLoaded(targetPackageName)) {

            intent.setClassName("com.android.mlplugin",
                    "com.android.mlplugin.StubActivity");
            intent.putExtra("key_is_plugin", true);
            intent.putExtra("key_package", targetPackageName);
            intent.putExtra("key_activity", targetClassName);
        }

        try {
            Method execStartActivity = Instrumentation.class.getDeclaredMethod(
                    "execStartActivity", Context.class, IBinder.class, IBinder.class,
                    Activity.class, Intent.class, int.class, Bundle.class);
            execStartActivity.setAccessible(true);
            return (ActivityResult) execStartActivity.invoke(mBase, who,
                    contextThread, token, target, intent, requestCode, options);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MALEI",e.toString()+"");
            throw new RuntimeException("do not support!!!" + e.getMessage());
        }
    }

    private boolean isPluginLoaded(String packageName) {
        return PluginManger.getLoadedPluginApk() != null;
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (hookToPluginActivity(intent)) {
            String targetClassName = intent.getComponent().getClassName();
            // TODO: 2022/4/26  获取当前目标activity的PluginApp对象实例
            PluginApp pluginApp = PluginManger.getLoadedPluginApk();
            Activity activity = mBase.newActivity(pluginApp.mClassLoader, targetClassName, intent);
            activity.setIntent(intent);

            // TODO: 2022/4/26  替换资源信息
            setField(ContextThemeWrapper.class, activity, "mResources", pluginApp.mResources);
            return activity;
        }
        return super.newActivity(cl, className, intent);
    }

    public static void setField(Class clazz, Object target, String field, Object object) {
        try {
            Field f = clazz.getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 将intent换回原来的Activity
    public boolean hookToPluginActivity(Intent intent) {

        if (intent.getBooleanExtra(KEY_IS_PLUGIN, false)) {
            String pkg = intent.getStringExtra(KEY_PACKAGE);
            String activity = intent.getStringExtra(KEY_ACTIVITY);
            intent.setClassName(pkg, activity);
            return true;
        }
        return false;
    }
}
