package aa;

import com.yy.mobile.whisper.Immutable;

import java.util.*;

public class A {

    @Immutable
    protected List<String> list = new ArrayList<>();

    public Set<String> a(){ //should lint
        @Immutable
        Set<String> set = new TreeSet<String>(){
            {
                add("33");
                add("22");
            }
        };
        return set;
    }

    public Set<String> b(){ //should not lint
        Set<String> set = new TreeSet<String>(){
            {
                add("33");
                add("22");
            }
        };
        return set;
    }

    public List<String> c() { //should lint
        List<String> local = list;
        return local;
    }
}