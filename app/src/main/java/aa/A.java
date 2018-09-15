package aa;

import com.yy.mobile.whisper.Immutable;

import java.util.*;

public class A {

    @Immutable
    protected Map<String, String> map = new HashMap<String, String>() {
        {
            put("a", "b");
        }
    };

    Collection<String> list = map.values(); //should lint

    public void a(@Immutable Collection<String> list) {
        for (String a : list) {
            System.out.println(a);
        }
        b(map); //should lint
    }

    public void b(Map<String, String> map) {
        c(this.map);
        throw new RuntimeException(map.keySet().toString());
    }

    public void c(@Immutable Map<? extends CharSequence, String> map) {
        a(map.values());
        a(list);
    }
}