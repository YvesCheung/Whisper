package hide.demo;

/**
 * @author YvesCheung
 * 2018/9/18.
 *
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
