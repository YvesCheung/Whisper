package hide.demo;

/**
 * Created by 张宇 on 2018/9/18.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
public class ClassTest2 {

    public ClassTest2() {

        ClassNeedToHide.INSTANCE.setDebuggable(true);
    }
}
