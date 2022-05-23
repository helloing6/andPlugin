package com.ml.plugin1.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import com.ml.plugin1.db.DBHelper;

public class MyContentProvider extends ContentProvider {
    public static final String TAG = "MALEI";
    private Context mContext;
    DBHelper mDbHelper = null;
    SQLiteDatabase db = null;
    // 设置ContentProvider的仅有标识
    public static final String AUTOHORITY = "com.ml.plugin1";
    public static final int User_Code = 1;
    // UriMatcher类运用:在ContentProvider 中注册URI
    private static final UriMatcher mMatcher;

    static {
        // 初始化
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // 若URI资源途径 = content://com.example.myprovider/user ，则回来注册码User_Code
        mMatcher.addURI(AUTOHORITY, "user", User_Code);
    }

    // 以下是ContentProvider的6个办法

    /**
     * 初始化ContentProvider
     */
    @Override
    public boolean onCreate() {
        mContext = getContext();
        // 在ContentProvider创立时对数据库进行初始化
        // 留心：运行在主线程，不能做耗时操作
        mDbHelper = new DBHelper(getContext());
        db = mDbHelper.getWritableDatabase();
        // 初始化两个表的数据(先清空两个表,再各参加一个记载)
        db.execSQL("delete from user");
        db.execSQL("insert into user values(1,'张三');");
        db.execSQL("insert into user values(2,'李四');");
        Log.d(TAG, "onCreate");
        return true;
    }

    /**
     * 添加数据
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // 依据URI匹配 URI_CODE，然后匹配ContentProvider中相应的表名
        String table = getTableName(uri);
        // 向该表添加数据
        db.insert(table, null, values);
        // 当该URI的ContentProvider数据产生改动时，告知外界（即拜访该ContentProvider数据的拜访者）
        mContext.getContentResolver().notifyChange(uri, null);
        Log.d(TAG, "insert uri:" + uri);
        return uri;
    }

    /**
     * 查询数据
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // 依据URI匹配 URI_CODE，然后匹配ContentProvider中相应的表名
        String table = getTableName(uri);
        Log.d(TAG, "query uri:" + uri);
        // 查询数据
        return db.query(table, projection, selection, selectionArgs, null, null, sortOrder, null);
    }

    /**
     * 更新数据
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.d(TAG, "update");
        return 0;
    }

    /**
     * 删去数据
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete");
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        Log.d(TAG, "getType");
        return null;
    }

    /**
     * 依据URI匹配 URI_CODE，然后匹配ContentProvider中相应的表名
     */
    private String getTableName(Uri uri) {
        String tableName = null;
        switch (mMatcher.match(uri)) {
            case User_Code:
                tableName = DBHelper.USER_TABLE_NAME;
                break;
        }
        Log.d(TAG, "getTableName uri:" + uri);
        return tableName;
    }
}