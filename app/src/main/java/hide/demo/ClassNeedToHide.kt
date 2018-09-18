package hide.demo

import android.util.Log
import com.yy.mobile.whisper.Hide

/**
 * Created by 张宇 on 2018/9/18.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
object ClassNeedToHide {

    var debuggable = false
        @Hide(friend = ["hide.demo.ClassTest", "ClassTest2"])
        set(value) {
            field = value
        }

    fun notifyError(str: String) {
        if (debuggable) {
            throw RuntimeException(str)
        } else {
            Log.e("Whisper", str)
        }
    }
}