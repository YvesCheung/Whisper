package aa;

import com.yy.mobile.whisper.Immutable;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class A {

    @Immutable
    public final Map<Long, String> field;

    @Immutable
    public final Map<Long, String> field2 = new TreeMap<>();

    private Map<Long, String> field3 = field2; //should lint

    public A() {
        Map<Long, String> local = new HashMap<>();
        local.put(3L, "2");
        field = local;
    }
}