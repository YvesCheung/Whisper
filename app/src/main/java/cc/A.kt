package cc

import com.yy.mobile.whisper.UseWith

class A {

    @UseWith("deInit")
    fun build(): B {
        return B()
    }
}