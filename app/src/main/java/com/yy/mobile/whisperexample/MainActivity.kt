package com.yy.mobile.whisperexample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.yy.mobile.whisper.Hide

class MainActivity : AppCompatActivity() {

    private val test1 = MyClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        test1.myMethod()

        //test1.finalValue = 3
    }

    @Hide
    var a = 3
}
