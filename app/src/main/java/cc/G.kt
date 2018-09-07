package cc

/**
 * Created by 张宇 on 2018/9/7.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
class G {

    val a = A()

    fun aInit() {
        with(A()) {
            val unused1 = init()
            val unused2 = aInit()
        }

        with(a) {
            val unused3 = init()
            val unused4 = aInit()
        }
    }
}