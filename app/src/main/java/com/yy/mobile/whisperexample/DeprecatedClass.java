package com.yy.mobile.whisperexample;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.yy.mobile.whisper.DeprecatedBy;
import com.yy.mobile.whisper.NeedInfo;
import com.yy.mobile.whisper.NeedWarning;


/**
 * Created by 张宇 on 2018/8/11.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
@SuppressWarnings("WeakerAccess")
public class DeprecatedClass {

    @DeprecatedBy(replaceWith = "methodV2(%2$s, %1$s, null)")
    public int methodV1(int argue1, @NonNull String argue2) {
        return methodV2(argue2, argue1, null);
    }

    public int methodV2(@NonNull String argue2, int argue1, @Nullable String argue3) {
        return 2;
    }

    @DeprecatedBy(
            replaceWith = "newMethod(%s)",
            receiver = "com.yy.mobile.whisperexample.Utils",
            message = "这个方法有bug，改用 Utils.newMethod 啦",
            level = DeprecatedBy.Level.Error)
    public void oldMethod(int somethingUseful, double somethingUseless) {
        Log.i("Whisper", "i am old method(" + somethingUseful + ")");
    }
}
