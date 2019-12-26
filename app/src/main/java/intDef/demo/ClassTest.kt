package intDef.demo

import com.yy.mobile.whisper.IntDef
import com.yy.mobile.whisper.StringDef

/**
 * @author YvesCheung
 * 2019-12-24
 */
class ClassTest {

    fun checkInt(@IntDef(1, 3, 4) param: Int) {}

    @IntDef(1, 3)
    private fun mustBe1Or3(): Int = 2

    fun checkIntArray(@IntDef(1, 3, 4) vararg param: Int) {}

    fun checkString(@StringDef("hello", "world") param: String) {}

    companion object {

        @JvmStatic
        fun main(vararg args: String) {
            val instance = ClassTest()

            instance.checkInt(instance.mustBe1Or3())

            instance.checkInt(1)

            instance.checkIntArray(6, 3, 4, 5)

            instance.checkString("hello world")

            instance.checkString("hello")
        }
    }
}