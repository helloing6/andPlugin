package com.android.mlplugin.service;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;


import com.android.mlplugin.App;
import com.android.mlplugin.plugin.PluginManger;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

/**
 * 自定义Handler.Callback类
 */
public class HandlerCallbackOfActivityThread implements Handler.Callback{

    private final Handler mBase;

    public HandlerCallbackOfActivityThread(Handler mH) {
        mBase = mH;
    }

    /**
     * msg = {
     *     when=-1ms
     *     what=114
     *     obj=CreateServiceData{
     *              token=android.os.BinderProxy@eeb323d
     *              className=com.ml.mplugin_service.ProxyService
     *              packageName=com.ml.mplugin_service
     *              intent=null}
     *     target=android.app.ActivityThread$H }
     * @param
     */
    @Override
    public boolean handleMessage(@NonNull Message msg) {
        Log.e("MALEI","HandlerCallback ==> " + msg.toString());
        int what = msg.what;
        Object object = msg.obj;

        switch (what){
            case 114: //拦截startService()方法
                handleCreateService(object);
                break;
        }
        mBase.handleMessage(msg);
        return true;
    }

    /**
     */
    private void handleCreateService(Object object) {
        Log.e("MALEI","obj ==> " + object.toString());
        try {
            //  ActivityThread 中的类 CreateServiceData
            Field info = object.getClass().getDeclaredField("info");
            info.setAccessible(true);

            // 获取CreateServiceData对象中的ServiceInfo对象实例
            ServiceInfo serviceInfo =  (ServiceInfo) info.get(object);
            String hostServiceName = serviceInfo.name;
            Log.e("MALEI","HandlerCallback => 代理类 hostServiceName = " + hostServiceName);
            //类加载器的替换
            replaceClassloader();

            // 这个地方替换为真实跳转的
            serviceInfo.name = "com.ml.plugin1.PluginService";
            serviceInfo.applicationInfo.packageName = "com.ml.plugin1";
        }catch (Exception ex){
            Log.e("MALEI","handleCreateService = "+ex);
        }
    }

    private void replaceClassloader() {
        try {
            /*--------获取ActivityThread对象-----------------*/
            // 获取ActivityThread类对象
            Class gActivityThreadClass = Class.forName("android.app.ActivityThread");
            // 获取ActivityThread类中的sCurrentActivityThread参数
            Field gCurrentActivityThread = gActivityThreadClass.getDeclaredField("sCurrentActivityThread");
            gCurrentActivityThread.setAccessible(true);
            // sCurrentActivityThread因为是静态参数，所以直接获取。到这里我们就通过反射获取到了当前进程ActivityThread的实例对象
            Object rCurrentActivityThread = gCurrentActivityThread.get(gActivityThreadClass);

            /*--------获取ActivityThread对象中的mPackages-----------------*/
            Field mPackagesField = gActivityThreadClass.getDeclaredField("mPackages");
            mPackagesField.setAccessible(true);
            if (null == mPackagesField) {
                Log.i("MALEI", "get mPackages field failed");
                return;
            }
            ArrayMap mPackages = (ArrayMap) mPackagesField.get(rCurrentActivityThread);
            if (null == mPackages) {
                Log.i("MALEI", "can not get mPackages");
                return;
            }

            /*--------获取当前插件保存的位置----------------*/
            String apkPath = PluginManger.getApkPath();

            //ApplicationInfo 通过它可以得到一个应用基本信息，这些信息是从AndroidManifest.xml的< application  标签获取的
            ApplicationInfo applicationInfo = generateApplicationInfo(apkPath);
            if(applicationInfo != null){
                Class<?> gCompatibilityInfo = Class.forName("android.content.res.CompatibilityInfo");
                Field gINFO = gCompatibilityInfo.getDeclaredField("DEFAULT_COMPATIBILITY_INFO");
                gINFO.setAccessible(true);
                Object rCompatibilityInfo =gINFO.get(gCompatibilityInfo);

                // 调用ActivityThread 类中的getPackageInfo()方法，返回LoadedApk
                RefInvoke.Reflect reflect = RefInvoke.on(
                        rCurrentActivityThread,
                        "getPackageInfo",
                        ApplicationInfo.class,
                        gCompatibilityInfo, int.class);
                Object loadedApk = reflect.invoke(applicationInfo, rCompatibilityInfo, Context.CONTEXT_INCLUDE_CODE);

                //插件包名
                String pluginName = applicationInfo.packageName;
                if (!TextUtils.isEmpty(pluginName)) {
                    Log.i("MALEI", "ActivityThreadHandler => 插件包名： " + pluginName);
                    Field mClassLoader = loadedApk.getClass().getDeclaredField("mClassLoader");
                    mClassLoader.setAccessible(true);
                    mClassLoader.set(loadedApk, PluginManger.mPluginApp.mClassLoader);
                    mPackages.put(pluginName, new WeakReference<>(loadedApk));
                } else {
                    Log.i("MALEI", "get plugin pkg name failed");
                }
            }
        }catch (Exception ex){
            Log.e("MALEI",""+ex);
        }
    }

    private ApplicationInfo generateApplicationInfo(String pluginPath) {
        try {
            ApplicationInfo applicationInfo = getApplicationInfoByPackageArchiveInfo(pluginPath);
            if (null == applicationInfo) {
                Log.i("MALEI", "get applicationInfo failed");
                return null;
            }
            applicationInfo.sourceDir = pluginPath;
            applicationInfo.publicSourceDir = pluginPath;
            applicationInfo.uid = Process.myUid();
            return applicationInfo;
        } catch (Exception e) {
            Log.i("MALEI", "generateApplicationzInfo failed " + e.getMessage());
        }
        return null;
    }
    private ApplicationInfo getApplicationInfoByPackageArchiveInfo(String pluginPath) {
        PackageManager packageManager = App.getContext().getPackageManager();
        if (null == packageManager) {
            Log.i("MALEI", "get PackageManager failed");
            return null;
        }
        PackageInfo packageInfo = packageManager.getPackageArchiveInfo(pluginPath, 0);
        if (null == packageInfo) {
            Log.i("MALEI", "get packageInfo failed");
            return null;
        }
        return packageInfo.applicationInfo;
    }
}
