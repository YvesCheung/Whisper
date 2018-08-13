package com.yy.mobile.whisperexample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class KotlinActivity : AppCompatActivity() {

    private val test1 = MessageClass()
    private val test2 = DeprecatedClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        test1.myMethod()

        with(test2) {

            //在kotlin中使用 fix 暂时无法shorterName
            oldMethod(3, 4.5)

            methodV1(3, "2")
        }
    }
}
