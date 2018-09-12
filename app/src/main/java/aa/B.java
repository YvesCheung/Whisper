package aa;

import java.util.Collections;
import java.util.Map;

public class B {

    public A getA() {
        return new A();
    }

    Map<Long, String> field = getA().field; //should lint

    Map<Long, String> field2 = getA().field2; //should lint

    public void a() {
        getA().field.putAll(Collections.singletonMap(4L, "ll")); //should lint
    }
}