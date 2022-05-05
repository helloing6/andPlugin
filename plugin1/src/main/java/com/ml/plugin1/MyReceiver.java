package com.ml.plugin1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context,
                "接收到300万" + intent.getAction() + intent.getStringExtra("msg"),
                Toast.LENGTH_LONG).show();
    }

}