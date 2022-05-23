package com.ml.plugin1;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //动态注册广播插件直接可以使用
        registerReceiver(new MyReceiver(),new IntentFilter("baobao3"));

        this.findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setPackage("com.ml.plugin1");
                intent.setClassName("com.ml.plugin1",
                        "com.ml.plugin1.TwoActivity");
                startActivity(intent);
            }
        });

        this.findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                intent = new Intent(MainActivity.this,PluginService.class);
                startService(intent);
            }
        });

        this.findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stopService(intent);
            }
        });

        this.findViewById(R.id.btn4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendBroadcast(new Intent("baobao3"));
            }
        });

        this.findViewById(R.id.btn5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendBroadcast(new Intent("malei"));
            }
        });

        this.findViewById(R.id.btn6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                findContentProvider();
            }
        });

        this.findViewById(R.id.btn7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                testContentProvider();
            }
        });

    }

    private void testContentProvider() {
        ContentValues values = new ContentValues();
        values.put("name", "jianqiang");
        Uri newUri = getContentResolver().insert(Uri.parse("content://maleitest/"), values);
        Toast.makeText(this, "insert uri:" + newUri, Toast.LENGTH_LONG).show();
    }



    public void findContentProvider(){
        // 设置URI
        Uri uri_user = Uri.parse("content://com.ml.plugin1/user");
        // 刺进表中数据
        ContentValues values = new ContentValues();
        values.put("_id", 3);
        values.put("name", "王五");
        // 获取ContentResolver
        ContentResolver resolver = getContentResolver();
        // 经过ContentResolver 依据URI 向ContentProvider中刺进数据
        resolver.insert(uri_user, values);
        // 经过ContentResolver 向ContentProvider中查询数据
        Cursor cursor = resolver.query(uri_user, new String[]{"_id", "name"}, null, null, null);
        while (cursor.moveToNext()) {
        // 将表中数据悉数输出
            Log.d("MALEI","query user:" + cursor.getInt(0) + " " + cursor.getString(1));
        }
        // 封闭游标
        cursor.close();
    }


}