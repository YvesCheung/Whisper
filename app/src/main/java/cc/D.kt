package dd

import cc.A

class D {

    private lateinit var a: A

    fun deInit() {
        a = A().apply { init() }
        a.also {
            it.deInit()
        }
    }
}