package hide.demo;

/**
 * Created by 张宇 on 2018/9/18.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
public class ClassTest3 {

    public void init() {

        /**
         * This usage is illegal
         * because {@link ClassTest3} is not a friend for {@link ClassNeedToHide}
         */
        ClassNeedToHide.INSTANCE.setDebuggable(true);
    }
}
