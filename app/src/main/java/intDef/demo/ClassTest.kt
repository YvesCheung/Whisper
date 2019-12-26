package intDef.demo

import com.yy.mobile.whisper.IntDef
import com.yy.mobile.whisper.LongDef
import com.yy.mobile.whisper.StringDef

/**
 * @author YvesCheung
 * 2019-12-24
 */
class ClassTest {

    fun checkInt(@IntDef(1, 3, 4) vararg param: Int) {}

    fun checkString(@StringDef("hello", "world") vararg param: String) {}

    fun checkLong(@LongDef(4, 5) vararg param: Long) {}

    companion object {

        @JvmStatic
        fun main(vararg args: String) {
            val instance = ClassTest()

            instance.checkInt(1, 3, 4, 5, 6)

            instance.checkString("hello", "hello world")

            instance.checkLong(1, 4, 5, 6)
        }
    }
}