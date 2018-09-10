package aa;

import com.yy.mobile.whisper.Immutable;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by 张宇 on 2018/9/10.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
public class A {

    @Immutable
    private Map<String, String> map = new LinkedHashMap<>();

    public void a() {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey().equals(entry.getValue())) {
                entry.setValue("asd"); //should lint
            }
        }
    }

    public void b() {
        Collection<String> collection = map.values();
        if (collection.isEmpty()) {
            collection.add("asd"); //should lint
        }
        Iterator<String> it = collection.iterator();
        while (it.hasNext()) {
            if (it.next().equals("haha")) {
                it.remove();
            }
        }
    }
}
