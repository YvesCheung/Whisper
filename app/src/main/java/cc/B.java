package cc;

import com.yy.mobile.whisper.Immutable;

import java.util.ArrayList;
import java.util.List;

public class B {

    private A a = new A();

    void onCreate() {
        final A aa = new A();

        onCallback(new Callback() {
            @Override
            public void haha() {
            }
        });
    }

    void onCallback(Callback cb) {
        if (cb != null) {
        }
    }

    void deInit() {

    }

    interface Callback {
        void haha();
    }

    @Immutable
    private List<Integer> list = new ArrayList<>(10);

    private void check(@Immutable List<Integer> list) {
        list.add(3);
    }
}