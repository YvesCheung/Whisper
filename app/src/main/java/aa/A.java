package aa;

import com.yy.mobile.whisper.Immutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by 张宇 on 2018/9/10.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
public class A {

    @Immutable
    private List<String> list = new ArrayList<>();

    public void a() {
        list.add("haha");

        list.remove("haha");

        list.addAll(Collections.singleton("haha"));

        list.removeAll(Collections.singleton("haha"));

        list.clear();

        list.retainAll(Collections.singleton("haha"));

        list.iterator().remove();
    }
}
