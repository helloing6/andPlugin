package com.ml.plugin1.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    // 数据库名
    private static final String DATABASE_NAME = "com_ml_plugin1_provider.db";
    // 表名
    public static final String USER_TABLE_NAME = "user";
    //数据库版本号
    private static final int DATABASE_VERSION = 1;
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创立 用户表
        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + USER_TABLE_NAME
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT," + " name TEXT)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
