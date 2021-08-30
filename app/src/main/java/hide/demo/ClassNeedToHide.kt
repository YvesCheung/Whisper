package hide.demo

import android.util.Log
import com.github.yvescheung.whisper.Hide

/**
 * @author YvesCheung
 * 2018/9/18.
 *
 */
object ClassNeedToHide {

    var debuggable = false
        @Hide(friend = ["hide.demo.ClassTest", "ClassTest2"])
        set(value) {
            field = value
        }

    @Hide(friend = ["ClassTest"])
    fun notifyError(str: String) {
        if (debuggable) {
            throw RuntimeException(str)
        } else {
            Log.e("Whisper", str)
        }
    }
}