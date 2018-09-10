package aa;

import com.yy.mobile.whisper.Immutable;

import java.util.Collections;
import java.util.TreeMap;

public class B {

    @Immutable
    private TreeMap<Integer, String> map = new TreeMap<>();

    public void a() {
        map.putAll(Collections.unmodifiableMap(
                Collections.<Integer, String>emptyMap())); //should lint
    }

    public void b() {
        map.subMap(1, 10).remove(3); //should lint
    }

    public void c() {
        if (map.containsKey(3)) {
            for (Integer a : map.keySet()) {
                String value = map.get(a);
            }
        }
    }
}