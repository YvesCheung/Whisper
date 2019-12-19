package usewith.demo

/**
 * @author YvesCheung
 * 2019-12-19
 */
class ClassTest2 {

    private val instance = UseWithCase()

    private fun init() {
        instance.init()

        instance.addListener { }
    }
}