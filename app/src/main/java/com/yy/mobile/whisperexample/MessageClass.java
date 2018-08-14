package com.yy.mobile.whisperexample;

import com.yy.mobile.whisper.NeedInfo;
import com.yy.mobile.whisper.NeedWarning;

/**
 * Created by 张宇 on 2018/8/13.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
@SuppressWarnings("WeakerAccess")
public class MessageClass {

    @NeedWarning("我是 MyMethod 方法，请小心使用")
    public void myMethod() {

    }

    public double methodWithInfo(@NeedInfo("传递负数有惊喜") int parameter) {
        if (parameter < 0) {
            double a = theFieldThatNoLongerUse;

            a = theFieldThatNoLongerUse;

            theFieldThatNoLongerUse = 2.4;
        }
        return theFieldThatNoLongerUse;
    }

    @NeedWarning("不再使用的变量2")
    public int theFieldThatNoLongerUse2;

    @NeedInfo("不再使用" + "的变量")
    public double theFieldThatNoLongerUse = theFieldThatNoLongerUse2 + 3.3;

    public double a = theFieldThatNoLongerUse / 2 + annoMethod();

    @NeedWarning("不再使用的变量")
    public int annoMethod() {
        return 3;
    }
}
