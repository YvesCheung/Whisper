package cc;

public class B {

    private A a = new A();

    void onCreate() {
        a.init();
        final A aa = new A();
        aa.init();

        onCallback(new Callback() {
            @Override
            public void haha() {
                aa.aInit();
            }
        });
    }

    void onCallback(Callback cb) {
        if (cb != null) {
            A b = a;
            b.aInit();
        }
    }

    interface Callback {
        void haha();
    }
}