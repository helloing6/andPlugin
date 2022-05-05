package com.android.mlplugin.plugin;

import android.content.res.Resources;

/**
 * 插件的内存模型
 */
public class PluginApp {
    public Resources mResources;
    public ClassLoader mClassLoader;


    public PluginApp(Resources mResources, ClassLoader mClassLoader) {
        this.mResources = mResources;
        this.mClassLoader = mClassLoader;

    }
}