package usewith.demo;

/**
 * Created by 张宇 on 2018/9/19.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
public class ClassTest {

    private final UseWithCase instance = new UseWithCase();

    private void init() {
        instance.init();

        instance.addListener(new UseWithCase.Listener() {
            @Override
            public void doSomething() {
            }
        });
    }
}
