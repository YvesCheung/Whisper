package com.yy.mobile.whisperlint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestLintTask
import org.junit.Test

/**
 * Created by 张宇 on 2018/9/10.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
class WhisperImmutableTest {

    private val immutableAnnotation: TestFile = java("""
                package com.yy.mobile.whisper;

                import java.lang.annotation.Documented;
                import java.lang.annotation.ElementType;
                import java.lang.annotation.Retention;
                import java.lang.annotation.RetentionPolicy;
                import java.lang.annotation.Target;

                @Documented
                @Target({ElementType.FIELD,
                        ElementType.METHOD,
                        ElementType.PARAMETER})
                @Retention(RetentionPolicy.SOURCE)
                public @interface Immutable {
                }
    """.trimIndent())

    @Test
    fun `Check Java field @Immutable collection and modify`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.ArrayList;
                import java.util.Collections;
                import java.util.List;

                public class A {

                    @Immutable
                    private List<String> list = new ArrayList<>();

                    public void a() {
                        list.add("haha");

                        list.remove("haha");

                        list.addAll(Collections.singleton("haha"));

                        list.removeAll(Collections.singleton("haha"));

                        list.clear();

                        list.retainAll(Collections.singleton("haha"));

                        list.iterator().remove();
                    }
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("")
    }
}