package aa;

import com.yy.mobile.whisper.Immutable;

import java.util.*;

public class B extends A {

    public void d(Map<String, ? extends CharSequence> map) {
        b(this.map); //should lint
        e(this.map.values());
        e(map.values());
    }

    public void e(@Immutable Collection<? extends CharSequence> collection) {
        d(this.map); //should lint
        d(new HashMap<>(this.map));
    }
}