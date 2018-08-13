package com.yy.mobile.whisperexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class JavaActivity extends AppCompatActivity {

    private final MyClass test1 = new MyClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        test1.myMethod();

        int result = test1.methodV1(3, "oo");

        test1.oldMethod(3, 4.0);

        test1.methodWithInfo(23);
    }
}
