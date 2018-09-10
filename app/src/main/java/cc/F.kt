package cc

class F {

    private var b: B? = A().let { it.build() } //should not lint

    fun a() {
        b?.deInit()
    }

    fun b() {
        A().let {
            it.build() //should not lint
        }.also{
            it.deInit()
        }
    }
}