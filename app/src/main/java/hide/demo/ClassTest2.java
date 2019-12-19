package hide.demo;

/**
 * @author YvesCheung
 * 2018/9/18.
 *
 */
public class ClassTest2 {

    public ClassTest2() {

        ClassNeedToHide.INSTANCE.setDebuggable(true);
    }
}
