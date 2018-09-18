package deprecate.demo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yy.mobile.whisper.DeprecatedBy;

/**
 * Created by 张宇 on 2018/9/18.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
public class ClassNeedToDeprecate {

    @DeprecatedBy(
            replaceWith = "newMethod(%$2s, %$1s, null)",
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
