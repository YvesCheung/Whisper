package hint.demo

import com.github.yvescheung.whisper.NeedError
import com.github.yvescheung.whisper.NeedInfo
import com.github.yvescheung.whisper.NeedWarning

/**
 * @author YvesCheung
 * 2019-12-19
 */
class ClassTest2 {

    @NeedInfo("This method should show info")
    private fun info() {
        //Do Nothing.
    }

    @NeedWarning("This method should show warning")
    private fun warning() {
        //Do Nothing.
    }

    @NeedError("This method should show error")
    private fun error() {
        //Do Nothing.
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val instance = ClassTest2()
            instance.info()
            instance.warning()
            instance.error()
        }
    }
}