package com.android.mlplugin.plugin;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class PluginManger {

    // 一个插件
    public static PluginApp mPluginApp;
    private static String apkPath;

    /**
     * 加载完一个插件后，会把该apk的资源和类加载器保存到PluginApp中
     */
    public static boolean loadPluginApk(Activity context, String apkName) {

        //  /data/user/0/com.example.xmplugin2/files/... 目录
        apkPath = context.getFileStreamPath(apkName).getPath();
        // 第一步：将assets里的apk保存到私有目录下 /data/user/0/包名/files/
        moveAssetsToSD(context, apkName);
        // 第二步： 加载插件资源
        Resources pluginRes = loadResources(context, apkPath);
        // 第三步： 创建DexClassLoader
        DexClassLoader pluginClassLoader = createDexClassLoader(context, apkPath);
        mPluginApp = new PluginApp(pluginRes, pluginClassLoader);
        return mPluginApp != null;
    }

    public static String getApkPath() {
        return apkPath;
    }


    private boolean isPluginLoaded(String packageName) {
        // TODO 检查packageNmae是否匹配
        return mPluginApp != null;
    }

    public static PluginApp getLoadedPluginApk() {
        return mPluginApp;
    }

    /**
     * 创建一个插件的DexClassLoader
     */
    private static DexClassLoader createDexClassLoader(Context context, String apkPath) {
        File dexOutputDir = context.getDir("dex", Context.MODE_PRIVATE);
        return new DexClassLoader(apkPath, dexOutputDir.getAbsolutePath(),
                null, context.getClassLoader());

    }

    /**
     * 加载插件的资源
     */
    private static Resources loadResources(Activity context, String dexPath) {
        try {
            //通过反射获取宿主的AssetManager
            AssetManager mAssetManager = AssetManager.class.newInstance();
            // 通过反射获取AssetManager类对象中的addAssetPath方法
            Method addAssetPath = mAssetManager.getClass().getMethod("addAssetPath", String.class);
            // 执行该方法，这样就把我们插件中的数据保存到了宿主中
            addAssetPath.setAccessible(true);
            addAssetPath.invoke(mAssetManager, dexPath);
            return new Resources(mAssetManager,
                    context.getResources().getDisplayMetrics(),
                    context.getResources().getConfiguration());

        } catch (Exception e) {
            Log.e("MALEI", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 把Assets里面得文件复制到 /data/data/包名/files 私有目录下（该目录只有root才可看）
     */
    private static void moveAssetsToSD(Context context, String apkName) {

        //返回应用程序包的AssetManager实例
        AssetManager am = context.getAssets();
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            //要打开的资产的名称
            is = am.open(apkName);
            File extractFile = context.getFileStreamPath(apkName);
            fos = new FileOutputStream(extractFile);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = is.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeSilently(is);
            closeSilently(fos);
        }
    }

    private static void closeSilently(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Throwable e) {
            // ignore
        }
    }
}
