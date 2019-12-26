package intDef.demo;

import com.yy.mobile.whisper.IntDef;
import com.yy.mobile.whisper.StringDef;

/**
 * @author YvesCheung
 * 2019-12-26
 */
public class ClassTest2 {

    private void checkInt(@IntDef({1, 3, 5}) int param) {}

    private void checkIntArray(@IntDef({1, 3, 5}) int... param) {}

    private void checkString(@StringDef({"Hello", "World"}) String param) {}

    public static void main(String[] args) {
        ClassTest2 instance = new ClassTest2();

        instance.checkInt(6);

        instance.checkIntArray(1, 4, 6);

        instance.checkString("Hello World");
    }
}
