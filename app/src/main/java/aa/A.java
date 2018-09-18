package aa;

import android.support.annotation.Nullable;

import com.yy.mobile.whisper.Immutable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class A {

    @Immutable
    public List<String> a() {
        return new ArrayList<>();
    }

    public List<String> b() {
        return new LinkedList<>();
    }

    @Immutable
    protected List<String> c() {
        return a();
    }

    public void d(@Immutable @Nullable List<String> a) {
        //do nothing
    }

    public void e(List<String> a) {
        //do nothing
    }

    void f(List<String> a) {
        //do nothing
    }

    @Immutable
    private Set<String> g(@Immutable Set<String> set){
        return set;
    }
}