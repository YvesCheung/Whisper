package usewith.demo

/**
 * @author YvesCheung
 * 2019-12-23
 */
class ClassTest3 {

    val a: UseWithCase by lazy {
        UseWithCase().also { it.init() }
    }

    fun deInit() {
        val b = a
        val c = b
        c.apply { deInit() }
    }
}