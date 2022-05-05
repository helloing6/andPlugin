package com.ml.plugin1;


import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

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



    }
}