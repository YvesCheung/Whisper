package deprecate.demo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.yvescheung.whisper.DeprecatedBy;

/**
 * @author YvesCheung
 * 2018/9/18.
 *
 */
public class ClassNeedToDeprecate {

    @DeprecatedBy(
            replaceWith = "newMethod(%2$s, %1$s, null)",
            message = "This method is deprecated, use newMethod() instead.",
            level = DeprecatedBy.Level.Error
    )
    public void oldMethod(int param1, @NonNull String param2) {
        newMethod(param2, param1, null);
    }

    public void newMethod(@NonNull String param2, int param1, @Nullable String param3) {
        System.out.println("i am new method. param = " + param2 + param1);
    }

    @DeprecatedBy(replaceWith = "newMethod(%s)", receiver = "deprecate.demo.ClassInstead")
    public void oldMethod2(int param) {
        ClassInstead.newMethod(param);
    }
}
