package deprecate.demo;

/**
 * Created by 张宇 on 2018/9/18.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
public class ClassTest {

    private ClassNeedToDeprecate instance = new ClassNeedToDeprecate();

    public void init() {

        instance.oldMethod(3, "this is param2");

        instance.oldMethod2(4);
    }
}
