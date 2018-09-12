package aa;

import com.yy.mobile.whisper.Immutable;

import java.util.*;

public class B {

    private Set<String> newSet; //should lint

    @Immutable
    private Set<String> set;

    public void a() {

        set = new HashSet<>();

        newSet = set;
    }
}