package usewith.demo

import com.yy.mobile.whisper.UseWith

/**
 * @author YvesCheung
 * 2019-12-19
 */
class ClassTest2 {

    private val instance = SDK()

    private fun init() {
        instance.init()
    }

    private fun unInit() {
        //instance.deInit()
    }

    class SDK {

        @UseWith("deInit")
        fun init() {
            //init sdk
        }

        fun deInit() {
            //Don't forget deInit!!
        }
    }
}

