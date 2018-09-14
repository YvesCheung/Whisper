package aa;

import java.util.LinkedList;
import java.util.List;

public class B extends A {

    @Override
    public List<Integer> a(LinkedList<Integer> list) { //should lint
        return super.a(list);
    }
}