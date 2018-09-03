package com.yy.mobile.whisperexample

import com.yy.mobile.whisper.Hide

/**
 * Created by 张宇 on 2018/9/3.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
class TheClassShouldBeHide {

    @Hide(friend = ["com.yy.mobile.Asd", "com.yy.mobile.NewActivity"])
    fun method(): Int {
        return 4
    }
}
