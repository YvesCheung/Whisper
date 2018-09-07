package cc;

public class C {

    void a() {
        new A().init();
        new A().deInit(); //should lint
    }

    private A instance;

    void b() {
        instance = new A();

        instance.init(); //should not lint
    }

    void c() {
        instance.deInit();
    }
}