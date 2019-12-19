package usewith.demo;

/**
 * @author YvesCheung
 * 2018/9/19.
 *
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
