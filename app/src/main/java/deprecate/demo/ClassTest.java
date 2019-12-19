package deprecate.demo;

/**
 * @author YvesCheung
 * 2018/9/18.
 *
 */
public class ClassTest {

    private ClassNeedToDeprecate instance = new ClassNeedToDeprecate();

    public void init() {

        instance.oldMethod(3, "this is param2");

        instance.oldMethod2(4);
    }
}
