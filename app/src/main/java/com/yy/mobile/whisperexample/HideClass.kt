package com.yy.mobile.whisperexample

import com.yy.mobile.whisper.Hide

/**
 * Created by 张宇 on 2018/9/3.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
class HideClass {

    @Hide(friend = arrayOf("KotlinActivity"))
    internal var asd = 3

    @Hide(friend = ["com.yy.mobile.whisperexample.JavaActivity"])
    internal fun method() {

    }
}
