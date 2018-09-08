package cc

import com.yy.mobile.whisper.UseWith

class A {

    @UseWith("deInit")
    fun init() {
    }

    fun deInit() {
    }

    @UseWith("deInit")
    fun build(): B {
        return B()
    }
}