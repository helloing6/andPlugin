package com.android.mlplugin.service;

import android.content.ComponentName;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.android.mlplugin.App;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 动态代理 IActivityManager
 */
public class IActivityManagerInvocationHandler implements InvocationHandler {

    private final Object mBase; //原始对象

    public IActivityManagerInvocationHandler(Object rInstance) {
        mBase = rInstance;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.e("MALEI", "IActivityManagerInvocationHandler ==> 拦截方法 ： " + method.getName());
        int index = -1;
        for (int i = 0 ; i < args.length ; i++){
            Object obj = args[i];
            // 获取intent对象
            if(obj instanceof Intent){
                index = i;
                break;
            }
        }

        if (-1 == index) {
            Log.i("MALEI", "AMSHookHelperInvocationHandler not found intent in params");
            return method.invoke(mBase, args);
        }
        String name = method.getName();
        if (TextUtils.equals(name, "startService")
                || TextUtils.equals(name, "stopService")
                || TextUtils.equals(name, "bindIsolatedService") //我的sdk版本是这个
                || TextUtils.equals(name, "bindService")
        ) {
            Intent realIntent = (Intent) args[index]; //真正跳转的intent
            hookServiceOperate(realIntent);
        }
        return method.invoke(mBase, args);
    }

    /**
     * realIntent 上层应用所构造Intent对象
     * 该方法只是拦截了启动服务时候，对目标服务改为了代理服务
     * 替换工作
     */
    private void hookServiceOperate(Intent realIntent) {

        if(realIntent != null){
            ComponentName pluginComponentName = realIntent.getComponent();
            Log.e("MALEI","IActivityManagerInvocationHandler ==> 真实的ComponentName "  + pluginComponentName.toString());

            if (null != pluginComponentName) {
                String pluginServiceName = pluginComponentName.getClassName(); //真实启动的服务类
                String pluginPackageName =pluginComponentName.getPackageName(); //启动的包名
                // 把目标信息保存到intent中
                realIntent.putExtra("pluginServiceName",pluginServiceName);
                realIntent.putExtra("pluginPackageName",pluginPackageName);

                //*****这个地方开始创建代理ComponentName，将启动的服务改为了代理服务****/
                //获取要启动的插件Service所对应的宿主Service名字哦
                String hostServiceName = "com.android.mlplugin.service.StubService";
                ComponentName componentName = new ComponentName(App.getContext().getPackageName(), hostServiceName);
                realIntent.setComponent(componentName);
                Log.e("MALEI","IActivityManagerInvocationHandler ==> 目标Service代理替换完毕 "  );
            }
        }
    }
}
