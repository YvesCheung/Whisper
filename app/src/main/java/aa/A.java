package aa;

import android.support.annotation.Nullable;

import com.yy.mobile.whisper.Immutable;

import java.util.concurrent.ConcurrentLinkedDeque;

public class A {

    @Immutable
    private int a = 3;

    @Immutable
    private final StringBuilder sb = new StringBuilder();

    @Nullable
    @Immutable
    private final ConcurrentLinkedDeque a() {
        return null;
    }

    @Immutable
    private final void b() {
    }
}