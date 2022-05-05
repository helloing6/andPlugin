package com.ml.plugin1;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;

public class TwoActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new Button(this));

    }
}
