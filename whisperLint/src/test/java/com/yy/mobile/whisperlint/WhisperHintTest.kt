package com.yy.mobile.whisperlint

import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

/**
 * Created by 张宇 on 2018/8/14.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
class WhisperHintTest {

    private val needErrorAnnotationFile = java("""
        |package com.yy.mobile.whisper;
        |import java.lang.annotation.Documented;
        |import java.lang.annotation.ElementType;
        |import java.lang.annotation.Inherited;
        |import java.lang.annotation.Retention;
        |import java.lang.annotation.RetentionPolicy;
        |import java.lang.annotation.Target;
        |
        |@Documented
        |@Target({ElementType.FIELD,ElementType.METHOD,ElementType.TYPE,ElementType.PARAMETER,
        |   ElementType.CONSTRUCTOR,ElementType.ANNOTATION_TYPE})
        |@Retention(RetentionPolicy.SOURCE)
        |@Inherited
        |public @interface NeedError {
        |
        |   String value();
        |}
        |
        """.trimMargin())!!

    @Test
    fun `Check Need Error Method`() {

        lint().files(
            needErrorAnnotationFile,
            java("""
                |package com.yy.mobile;
                |import com.yy.mobile.whisper.NeedError;
                |
                |public class TestClass {
                |
                |   @NeedError("i am test")
                |   public void function() {
                |
                |   }
                |
                |   TestClass(){
                |       function();
                |   }
                |}
            """.trimMargin()),
            java("""
                |package com.yy.mobile;
                |
                |public class TestClass2 {
                |   public TestClass2(){
                |       new TestClass().function();
                |   }
                |}
            """.trimMargin()))
            .detector(WhisperHintDetector())
            .issues(*WhisperHintDetector.getIssue())
            .run()
            .expect("src/com/yy/mobile/TestClass.java:12: Error: i am test [WhisperError]\n" +
                "       function();\n" +
                "       ~~~~~~~~~~\n" +
                "src/com/yy/mobile/TestClass2.java:5: Error: i am test [WhisperError]\n" +
                "       new TestClass().function();\n" +
                "       ~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "2 errors, 0 warnings")
    }

}