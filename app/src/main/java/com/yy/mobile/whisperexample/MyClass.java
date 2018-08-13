package com.yy.mobile.whisperexample;

import android.support.annotation.IntRange;

import com.yy.mobile.whisper.Hide;
import com.yy.mobile.whisper.NeedWarning;

import org.jetbrains.annotations.TestOnly;


/**
 * Created by 张宇 on 2018/8/11.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
public class MyClass {

    @Hide
    int finalValue = 3;

    @Hide
    @TestOnly
    void testMethod(@IntRange(from = 3, to = 4) int argu) {
        finalValue = argu;
    }

    @NeedWarning("我是 MyMethod 方法，请小心使用")
    public void myMethod() {

    }
}
