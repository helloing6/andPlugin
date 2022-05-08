package com.android.mlplugin.broad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.android.mlplugin.plugin.PluginApp;
import com.android.mlplugin.plugin.PluginManger;

import java.io.File;
import java.util.List;

public final class ReceiverHelper {

    private static final String TAG = "ReceiverHelper";

    /**
     * 解析插件Apk文件中的 <receiver>, 并存储起来
     * 将插件中静态广播都扫描出来
     * @param apkFile
     * @throws Exception
     */
    public static void preLoadReceiver(Context context, File apkFile) {
        // 首先调用parsePackage获取到apk对象对应的Package对象
        Object packageParser = RefInvoke2.createObject("android.content.pm.PackageParser");
        Class[] p1 = {File.class, int.class};
        Object[] v1 = {apkFile, PackageManager.GET_RECEIVERS};
        Object packageObj = RefInvoke2.invokeInstanceMethod(packageParser, "parsePackage", p1, v1);

        // 读取Package对象里面的receivers字段,注意这是一个 List<Activity> (没错,底层把<receiver>当作<activity>处理)
        // 接下来要做的就是根据这个List<Activity> 获取到Receiver对应的 ActivityInfo (依然是把receiver信息用activity处理了)
        List receivers = (List) RefInvoke2.getFieldObject(packageObj, "receivers");

        for (Object receiver : receivers) {
            registerDynamicReceiver(context, receiver);
        }
    }

    // 解析出 receiver以及对应的 intentFilter
    // 手动注册Receiver
    public static void registerDynamicReceiver(Context context, Object receiver) {
        //取出receiver的intents字段
        List<? extends IntentFilter> filters = (List<? extends IntentFilter>) RefInvoke2.getFieldObject(
                "android.content.pm.PackageParser$Component", receiver, "intents");

        try {
            // 把解析出来的每一个静态Receiver都注册为动态的
            for (IntentFilter intentFilter : filters) {
                Log.e("MALEI",intentFilter.toString());
                ActivityInfo receiverInfo = (ActivityInfo) RefInvoke2.getFieldObject(receiver, "info");

                PluginApp plugin = PluginManger.getLoadedPluginApk();
                BroadcastReceiver broadcastReceiver  = (BroadcastReceiver)plugin.mClassLoader.loadClass(receiverInfo.name).newInstance();

//                BroadcastReceiver broadcastReceiver = (BroadcastReceiver) RefInvoke.createObject(receiverInfo.name);
                context.registerReceiver(broadcastReceiver, intentFilter);
            }
        } catch (Exception e) {
            Log.e("MALEI",e.toString());
            e.printStackTrace();
        }
    }
}
