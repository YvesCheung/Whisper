package aa;

import java.util.ArrayList;
import java.util.List;

public class B extends A {

    public List<String> d() { //should lint
        List<String> local = list;
        return local;
    }

    public List<String> e() { //should not lint
        List<String> local = new ArrayList<>(list);
        return local;
    }
}