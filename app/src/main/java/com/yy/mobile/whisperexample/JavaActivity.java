package com.yy.mobile.whisperexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class JavaActivity extends AppCompatActivity {

    private final DeprecatedClass test1 = new DeprecatedClass();
    private final MessageClass test2 = new MessageClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int result = test1.methodV1(3, "oo");

        test1.oldMethod(3, 4.0);

        test2.myMethod();

        test2.methodWithInfo(23);
    }
}
