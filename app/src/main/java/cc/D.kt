package cc

class D {

    private var b = B()

    fun a() {
        b = A().build() //should not lint
    }

    fun b() {
        b.deInit()
    }
}