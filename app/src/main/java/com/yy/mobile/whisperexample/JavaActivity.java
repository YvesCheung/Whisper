package com.yy.mobile.whisperexample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.yy.mobile.NewActivity;

public class JavaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new TheClassShouldBeHide().method();
        new NewActivity();
    }
}
