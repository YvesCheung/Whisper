package com.yy.mobile.whisperexample;

import com.yy.mobile.whisper.NeedInfo;
import com.yy.mobile.whisper.NeedWarning;

/**
 * Created by 张宇 on 2018/8/13.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
public class MessageClass {

    @NeedWarning("我是 MyMethod 方法，请小心使用")
    public void myMethod() {

    }

    public void methodWithInfo(@NeedInfo("传递负数有惊喜") int parameter) {

    }

}
