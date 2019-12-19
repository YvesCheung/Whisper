package hint.demo

import com.yy.mobile.whisper.NeedError
import com.yy.mobile.whisper.NeedInfo
import com.yy.mobile.whisper.NeedWarning

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