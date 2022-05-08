package com.android.mlplugin.service;

import android.content.pm.PackageInfo;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class IPackageManagerHandler implements InvocationHandler {
    private final Object mBase;

    public IPackageManagerHandler(Object base) {
        mBase = base;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        if (TextUtils.equals("getPackageInfo", method.getName())) {
            Log.i("MALEI", "current method is getPackageInfo");
            return new PackageInfo();
        }
        return method.invoke(mBase, objects);
    }
}
