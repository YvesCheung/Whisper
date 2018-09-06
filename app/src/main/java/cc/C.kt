package ccc

import cc.A

class C {

    val a = A()

    init {
        a.init() //should Lint

        a.let {
            aInit()
        }
    }

    fun aInit() {
        var b: A = A().also {
            it.init() //should lint
        }

        b = A().also {
            it.init() //should not lint
        }

        b.let { it.aInit() }
        b.apply { aInit() }
        b.run { aInit() }
        b.also { it.aInit() }
    }
}