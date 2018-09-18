package aa;

import java.util.List;
import java.util.Set;

public class B extends A {

    @Override
    public List<String> a() {
        return super.a();
    }

    @Override
    public List<String> b() {
        return super.b();
    }

    @Override
    protected List<String> c() {
        return super.c();
    }

    @Override
    public void d(List<String> a) {
        super.d(a);
    }

    @Override
    public void e(List<String> a) {
        super.e(a);
    }

    @Override
    void f(List<String> a) {
        super.f(a);
    }

    private Set<String> g(Set<String> set) {
        return set;
    }
}