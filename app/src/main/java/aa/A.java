package aa;

import com.yy.mobile.whisper.Immutable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by 张宇 on 2018/9/10.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
public class A {

    @Immutable
    private LinkedBlockingDeque<Long> que = new LinkedBlockingDeque<>();

    public void a() {
        que.drainTo(new ArrayList<Long>()); //should lint

        try {
            que.putLast(3L); //should lint
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void b() {
        Iterator<Long> it = que.descendingIterator();
        for (; it.hasNext(); ) {
            if (it.next() > 3L) {
                it.remove(); //should lint
            }
        }
    }
}
