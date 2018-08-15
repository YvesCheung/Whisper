package com.yy.mobile.whisperlint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

/**
 * Created by 张宇 on 2018/8/14.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
class WhisperHintTest {

    private val needErrorAnnotationFile: TestFile = java("""
                |package com.yy.mobile.whisper;
                |
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
                """.trimMargin())

    private val needInfoAnnotationFile: TestFile = java("""
                |package com.yy.mobile.whisper;
                |
                |import java.lang.annotation.Documented;
                |import java.lang.annotation.ElementType;
                |import java.lang.annotation.Inherited;
                |import java.lang.annotation.Retention;
                |import java.lang.annotation.RetentionPolicy;
                |import java.lang.annotation.Target;
                |
                |@Documented
                |@Target({ElementType.FIELD,ElementType.METHOD,ElementType.TYPE,
                |ElementType.PARAMETER,ElementType.CONSTRUCTOR,ElementType.ANNOTATION_TYPE})
                |@Retention(RetentionPolicy.SOURCE)
                |@Inherited
                |public @interface NeedInfo {
                |
                |   String value();
                |}""".trimMargin())

    private val needWarningAnnotationFile: TestFile = java("""
                |package com.yy.mobile.whisper;
                |
                |import java.lang.annotation.Documented;
                |import java.lang.annotation.ElementType;
                |import java.lang.annotation.Inherited;
                |import java.lang.annotation.Retention;
                |import java.lang.annotation.RetentionPolicy;
                |import java.lang.annotation.Target;
                |
                |@Documented
                |@Target({ElementType.FIELD,ElementType.METHOD,ElementType.TYPE,
                |ElementType.PARAMETER,ElementType.CONSTRUCTOR,ElementType.ANNOTATION_TYPE})
                |@Retention(RetentionPolicy.SOURCE)
                |@Inherited
                |public @interface NeedWarning {
                |
                |   String value();
                |}""".trimMargin())

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
            .run()
            .expect("src/com/yy/mobile/TestClass.java:12: Error: i am test [WhisperError]\n" +
                "       function();\n" +
                "       ~~~~~~~~~~\n" +
                "src/com/yy/mobile/TestClass2.java:5: Error: i am test [WhisperError]\n" +
                "       new TestClass().function();\n" +
                "       ~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "2 errors, 0 warnings")
    }

    @Test
    fun `Check Need Warning Method`() {

        lint().files(
            needWarningAnnotationFile,
            java("""
                |package com.yy.mobile;
                |
                |public class TestClass {
                |
                |   @com.yy.mobile.whisper.NeedWarning("i am test")
                |   public int function() {
                |       return 3;
                |   }
                |
                |   TestClass(){
                |       int a = function();
                |   }
                |}
            """.trimMargin()),
            kotlin("""
                |package com.yy.mobile.whisper
                |
                |class TestKotlin {
                |
                |   private val aCls = com.yy.mobile.TestClass()
                |
                |   fun method():Int = aCls.function()
                |}
            """.trimMargin()))
            .detector(WhisperHintDetector())
            .run()
            .expect("src/com/yy/mobile/TestClass.java:11: Warning: i am test [WhisperWarning]\n" +
                "       int a = function();\n" +
                "               ~~~~~~~~~~\n" +
                "src/com/yy/mobile/whisper/TestKotlin.kt:7: Warning: i am test [WhisperWarning]\n" +
                "   fun method():Int = aCls.function()\n" +
                "                      ~~~~~~~~~~~~~~~\n" +
                "0 errors, 2 warnings")
    }

    @Test
    fun `Check Need Info field`() {

        lint().files(
            needInfoAnnotationFile,
            java("""
                |package com.whisper.mobile;
                |
                |import com.yy.mobile.whisper.NeedInfo;
                |
                |public class Holo {
                |
                |   @NeedInfo("hahaha "+   "asd")
                |   int field = 3;
                |
                |   public Holo(){
                |       field = 4;
                |   }
                |}
            """.trimMargin()),
            java("""
                |package com.whisper.mobile;
                |
                |public class HoloChild extends Holo {
                |
                |   @com.yy.mobile.whisper.NeedInfo("what the fuck")
                |   protected Holo parent = new Holo();
                |
                |   private void func(){
                |       int a = field;
                |   }
                |}
            """.trimMargin()),
            kotlin("""
                |package com.whisper.another.pkg
                |
                |import com.whisper.mobile.HoloChild
                |
                |class AnotherHolo : HoloChild() {
                |
                |   private fun ini() {
                |       val a = parent;
                |   }
                |}
            """.trimMargin()))
            .detector(WhisperHintDetector())
            .run()
            .expect("src/com/whisper/another/pkg/AnotherHolo.kt:8: Information: what the fuck [WhisperInfo]\n" +
                "       val a = parent;\n" +
                "               ~~~~~~\n" +
                "src/com/whisper/mobile/Holo.java:11: Information: hahaha asd [WhisperInfo]\n" +
                "       field = 4;\n" +
                "       ~~~~~\n" +
                "src/com/whisper/mobile/HoloChild.java:9: Information: hahaha asd [WhisperInfo]\n" +
                "       int a = field;\n" +
                "               ~~~~~\n" +
                "0 errors, 0 warnings")
    }
}