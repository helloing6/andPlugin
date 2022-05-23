package com.android.mlplugin.provider;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProviderHelper {

    /**
     * 解析Apk文件中的 <provider>, 并存储起来
     * 主要是调用PackageParser类的generateProviderInfo方法
     *
     * @param apkFile 插件对应的apk文件
     * @throws Exception 解析出错或者反射调用出错, 均会抛出异常
     */
    public static List<ProviderInfo> parseProviders(File apkFile) throws Exception {

        //获取PackageParser对象实例
        Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");
        Object packageParser = packageParserClass.newInstance();

        // 首先调用parsePackage获取到apk对象对应的Package对象
        Class[] p1 = {File.class, int.class};
        Object[] v1 = {apkFile, PackageManager.GET_PROVIDERS};
        Object packageObj = RefInvoke3.invokeInstanceMethod(packageParser, "parsePackage",p1, v1);

        // 读取Package对象里面的services字段
        // 接下来要做的就是根据这个List<Provider> 获取到Provider对应的ProviderInfo
        List providers = (List) RefInvoke3.getFieldObject(packageObj, "providers");

        // 调用generateProviderInfo 方法, 把PackageParser.Provider转换成ProviderInfo

        //准备generateProviderInfo方法所需要的参数
        Class<?> packageParser$ProviderClass = Class.forName("android.content.pm.PackageParser$Provider");
        Class<?> packageUserStateClass = Class.forName("android.content.pm.PackageUserState");
        Object defaultUserState = packageUserStateClass.newInstance();
        int userId = (Integer) RefInvoke3.invokeStaticMethod("android.os.UserHandle", "getCallingUserId");
        Class[] p2 = {packageParser$ProviderClass, int.class, packageUserStateClass, int.class};

        List<ProviderInfo> ret = new ArrayList<>();
        // 解析出intent对应的Provider组件
        for (Object provider : providers) {
            Object[] v2 = {provider, 0, defaultUserState, userId};
            //***第二步：把得到的Package对象转换为我们需要的ProviderInfo类型对象***
            ProviderInfo info = (ProviderInfo) RefInvoke3.invokeInstanceMethod(packageParser, "generateProviderInfo",p2, v2);
            ret.add(info);
        }

        return ret;
    }

    /**
     * 在进程内部安装provider, 也就是调用 ActivityThread.installContentProviders方法
     *
     * @param context you know
     * @param apkFile
     * @throws Exception
     */
    public static void installProviders(Context context, File apkFile) throws Exception {
        List<ProviderInfo> providerInfos = parseProviders(apkFile);
        //***第三步：把插件ContentProvider的packageName设置为当前apk的packageName
        for (ProviderInfo providerInfo : providerInfos) {
            providerInfo.applicationInfo.packageName = context.getPackageName();
        }
        //***第四步：把这些插件ContentProvider安装到宿主App中
        Object currentActivityThread = RefInvoke3.getStaticFieldObject("android.app.ActivityThread", "sCurrentActivityThread");

        Class[] p1 = {Context.class, List.class};
        Object[] v1 = {context, providerInfos};

        RefInvoke3.invokeInstanceMethod(currentActivityThread, "installContentProviders", p1, v1);
    }
}
