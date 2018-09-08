package cc;

import com.yy.mobile.whisper.Immutable;

import java.util.ArrayList;
import java.util.List;

public class B {

    private A a = new A();

    void onCreate() {
        a.init();
        final A aa = new A();
        aa.init();

        onCallback(new Callback() {
            @Override
            public void haha() {
                aa.deInit();
            }
        });
    }

    void onCallback(Callback cb) {
        if (cb != null) {
            A b = a;
            b.deInit();
        }
    }

    interface Callback {
        void haha();
    }

    @Immutable
    private List<Integer> list = new ArrayList<>(10);

    private void check(@Immutable List<Integer> list){
        list.add(3);
    }
}