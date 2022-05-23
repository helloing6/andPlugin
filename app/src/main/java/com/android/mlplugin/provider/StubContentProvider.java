package com.android.mlplugin.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class StubContentProvider extends ContentProvider {

    private static final String TAG = "StubContentProvider";

    public static final String AUTHORITY = "malei";

    private static final UriMatcher mMatcher;
    public static final int User_Code = 1;

    static {
        // 初始化
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // 若URI资源途径 = content://malei/user ，则回来注册码User_Code
        mMatcher.addURI(AUTHORITY, "user", User_Code);
    }


    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //noinspection ConstantConditions
        return getContext().getContentResolver().query(getRealUri(uri), projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //noinspection ConstantConditions
        return getContext().getContentResolver().insert(getRealUri(uri), values);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return getContext().getContentResolver().delete(getRealUri(uri), selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return getContext().getContentResolver().update(getRealUri(uri), values, selection, selectionArgs);
    }

    /**
     * 为了使得插件的ContentProvder提供给外部使用，我们需要一个StubProvider做中转；
     * 如果外部程序需要使用插件系统中插件的ContentProvider，不能直接查询原来的那个uri
     * 我们对uri做一些手脚，使得插件系统能识别这个uri；
     *
     * 这里的处理方式如下：
     *
     * 原始查询插件的URI应该为：
     * content://host_auth/plugin_auth/path/query
     * 例子 content://baobao222/jianqiang
     *
     * 如果需要查询插件，替换为：
     *
     * content://plugin_auth/path/query
     * 例子 content://jianqiang
     *
     * 也就是，我们把插件ContentProvider的信息放在URI的path中保存起来；
     * 然后在StubProvider中做分发。
     *
     * @param raw 外部查询我们使用的URI
     * @return 插件真正的URI
     */
    private Uri getRealUri(Uri raw) {
        String rawAuth = raw.getAuthority();
        if (!AUTHORITY.equals(rawAuth)) {
            Log.w(TAG, "rawAuth:" + rawAuth);
        }

        String uriString = raw.toString();
        uriString = uriString.replaceAll(rawAuth + '/', "com.ml.plugin1/");
        Uri newUri = Uri.parse(uriString);
        Log.i(TAG, "realUri:" + newUri);
        return newUri;
    }

}
