package com.ml.plugin1.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class TestContentProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
         Log.e("MALEI","===onCreate===");
        return true;
    }

    @Override
    public String getType(Uri uri) {
          Log.e("MALEI","~~getType~~");
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
         Log.e("MALEI",uri + "===insert===");
         Log.e("MALEI","values:" + values);
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String where,
                        String[] whereArgs, String sortOrder) {
         Log.e("MALEI",uri + "===query===");
         Log.e("MALEI","where:" + where);
        return null;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
         Log.e("MALEI",uri + "===delete===");
         Log.e("MALEI","where:" + where);
        return 1;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where,
                      String[] whereArgs) {
         Log.e("MALEI",uri + "===update===");
         Log.e("MALEI","where:" + where + ",values:" + values);
        return 2;
    }
}
