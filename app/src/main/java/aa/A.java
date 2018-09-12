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

    private List<String> list; //should lint

    public void a() {

        @Immutable
        List<String> localList = new ArrayList<>(Collections.singleton("haha"));

        localList.add("haha2"); //should lint

        list = localList;
        list.add("haha3"); //no necessary lint
    }
}
