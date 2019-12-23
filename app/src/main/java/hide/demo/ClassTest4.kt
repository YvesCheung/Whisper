package hide.demo

/**
 * @author YvesCheung
 * 2019-12-23
 */
object ClassTest4 {

    @JvmStatic
    fun main(vararg args: String) {

        ClassNeedToHide.debuggable = false

        ClassNeedToHide.notifyError("asd")
    }
}