package dd
import cc.A

class D {

    val a = A().apply { init() }

    fun deInit() {
        a.also {
            it.aInit()
        }
    }
}