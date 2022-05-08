package com.android.mlplugin.service;

import android.os.Handler;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * hook framework层的代码，替换为自定义类
 */
public class HookServiceManager {

    public static void init(){
        hookAMN();
        hookHandler();
        hookPkgManager();
    }

    /**
     * 这个方法就可以将目标Service替换为proxyService
     *
     * 1）hook住系统的ActivityManger，将IActivityManagerSingleton替换为自定义动态代理的新类
     * 2）在动态代理中，将Intent的ComponentName信息，替换为新的ComponentName信息，内部为代理服务
     *
     */
    public static void hookAMN() {
        try {
            //获取ActivityManager类
            Class<?> gActivityManagerCls = Class.forName("android.app.ActivityManager");
            //获取ActivityManager类中的IActivityManagerSingleton参数
            Field gIActivityManagerSingleton = gActivityManagerCls.getDeclaredField("IActivityManagerSingleton");
            gIActivityManagerSingleton.setAccessible(true);

            // 获取 gIActivityManagerSingleton实例因为是静态的可以直接拿Sing
            Object rIActivityManagerSingletonObj = gIActivityManagerSingleton.get(gActivityManagerCls);

            if (null != rIActivityManagerSingletonObj) {
                Class<?> gSingleton = Class.forName("android.util.Singleton");
                Field gInstanceField = gSingleton.getDeclaredField("mInstance");
                gInstanceField.setAccessible(true);
                Object rInstance = gInstanceField.get(rIActivityManagerSingletonObj);

                //动态代理
                Class<?> mIActivityManagerCls = Class.forName("android.app.IActivityManager");
                Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                        new Class[]{mIActivityManagerCls}, new IActivityManagerInvocationHandler(rInstance));
                // 将原始类替换为代理类
                gInstanceField.set(rIActivityManagerSingletonObj, proxy);
                Log.e("MALEI", "HookManager ==> IActivityManager 被替换");
            }else {
                Log.e("MALEI", "HookManager ==> IActivityManagerSingleton not exists");
                throw new Exception("IActivityManagerSingleton not exists");
            }
        }catch (Exception ex){
            Log.e("MALEI", "HookManager ==> hook ATM failed " + ex);
            ex.printStackTrace();
        }
    }

    /**
     * 该方法是hook住了ActivityThread中的Handler实例，然后替换为了自定义Handler中的Callback
     */
    public static void hookHandler() {
        try {
            // 获取进程 ActivityThread 的实例对象
            Class<?> gActivityThread = Class.forName("android.app.ActivityThread");
            Field gCurrentActivityThread = gActivityThread.getDeclaredField("sCurrentActivityThread");
            gCurrentActivityThread.setAccessible(true);
            Object rCurrentActivityThread = gCurrentActivityThread.get(gActivityThread);

            // 获取进程 ActivityThread对象中的Handler的实例对象
            Field gHField = gActivityThread.getDeclaredField("mH");
            gHField.setAccessible(true);
            Handler mH = (Handler)gHField.get(rCurrentActivityThread);

            Field gCallback = Handler.class.getDeclaredField("mCallback");
            gCallback.setAccessible(true);
            // 通过反射将Handler中的Callback替换为自定义的Callback
            gCallback.set(mH, new HandlerCallbackOfActivityThread(mH));
            Log.i("MALEI", "HookManager ==> hook H complete");
        } catch (Exception e) {
            Log.i("MALEI", "HookManager ==> hook H failed " + e);
        }
    }

    public static void hookPkgManager() {
        try {
            Class<?> activityThreadCls = RefInvoke.getClass("android.app.ActivityThread");
            Object sPackage = RefInvoke.on(activityThreadCls, "getPackageManager").invoke();

            Class<?> cls = RefInvoke.getClass("android.content.pm.IPackageManager");
            Object object = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{cls}, new IPackageManagerHandler(sPackage));

            RefInvoke.setStaticFieldValue(RefInvoke.getField(activityThreadCls, "sPackageManager"), activityThreadCls, object);
        } catch (Exception e) {
            Log.e("MALEI", "hook package manager failed " + e.getMessage());
        }
    }
}
