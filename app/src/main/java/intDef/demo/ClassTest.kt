package intDef.demo

import com.yy.mobile.whisper.IntDef
import com.yy.mobile.whisper.StringDef

/**
 * @author YvesCheung
 * 2019-12-24
 */
class ClassTest {

    fun checkInt(@IntDef(1, 3) param: Int) {}

    fun checkString(@StringDef("hello", "world") param: String) {}

    companion object {

        @JvmStatic
        fun main(vararg args: String) {
            val instance = ClassTest()

            instance.checkInt(4)

            instance.checkInt(1)

            instance.checkString("hello world")

            instance.checkString("hello")
        }
    }
}