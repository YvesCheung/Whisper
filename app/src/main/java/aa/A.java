package aa;

import com.yy.mobile.whisper.Immutable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class A {

    public void a(@Immutable Collection<String> list) {
        for (String a : list) {
            System.out.println(a);
        }
    }

    protected Long b(@Immutable Queue<Long> que) {
        c(que);
        return que.peek();
    }

    public void c(Queue<Long> queue) { //should lint
        b(queue);
        throw new UnsupportedOperationException("not implement");
    }

    public void d() {
        @Immutable
        Map<String, String> map = new HashMap<String, String>() {
            {
                put("a", "b");
            }
        };
        Collection<String> list = map.values();

        a(list); //should not lint

        e(map); //should lint
    }

    public void e(Map<String, String> map) {
        d();
        System.out.println(map);
    }
}