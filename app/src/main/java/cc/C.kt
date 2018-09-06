package ccc
import cc.A

class C {

    val a = A()

    init{
        a.init()

        unInit()
    }

    fun unInit() {
        with(A()){
            deInit()
        }
    }
}