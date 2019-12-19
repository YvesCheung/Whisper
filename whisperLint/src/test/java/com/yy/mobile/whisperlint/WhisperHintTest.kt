package com.yy.mobile.whisperlint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

/**
 * @author YvesCheung
 * 2018/8/14.
 *
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

    @Test
    fun `Check Kotlin method`() {
        lint().files(
            needInfoAnnotationFile,
            needWarningAnnotationFile,
            needErrorAnnotationFile,
            kotlin("""
                |package com.yy.mobile
                |
                |import com.yy.mobile.whisper.NeedError
                |import com.yy.mobile.whisper.NeedInfo
                |import com.yy.mobile.whisper.NeedWarning
                |
                |class ClassTest2 {
                |
                |   @NeedInfo("This method should show info")
                |   private fun info() {
                |       //Do Nothing.
                |   }
                |
                |   @NeedWarning("This method should show warning")
                |   private fun warning() {
                |       //Do Nothing.
                |   }
                |
                |   @NeedError("This method should show error")
                |   private fun error() {
                |       //Do Nothing.
                |   }
                |
                |   companion object {
                |
                |       @JvmStatic
                |       fun main(args: Array<String>) {
                |           val instance = ClassTest2()
                |           instance.info()
                |           instance.warning()
                |           instance.error()
                |       }
                |   }
                |}
            """.trimMargin()))
            .detector(WhisperHintDetector())
            .run()
            .expect("src/com/yy/mobile/ClassTest2.kt:31: Error: This method should show error [WhisperError]\n" +
                "           instance.error()\n" +
                "           ~~~~~~~~~~~~~~~~\n" +
                "src/com/yy/mobile/ClassTest2.kt:31: Error: This method should show error [WhisperError]\n" +
                "           instance.error()\n" +
                "           ~~~~~~~~~~~~~~~~\n" +
                "src/com/yy/mobile/ClassTest2.kt:29: Information: This method should show info [WhisperInfo]\n" +
                "           instance.info()\n" +
                "           ~~~~~~~~~~~~~~~\n" +
                "src/com/yy/mobile/ClassTest2.kt:29: Information: This method should show info [WhisperInfo]\n" +
                "           instance.info()\n" +
                "           ~~~~~~~~~~~~~~~\n" +
                "src/com/yy/mobile/ClassTest2.kt:30: Warning: This method should show warning [WhisperWarning]\n" +
                "           instance.warning()\n" +
                "           ~~~~~~~~~~~~~~~~~~\n" +
                "src/com/yy/mobile/ClassTest2.kt:30: Warning: This method should show warning [WhisperWarning]\n" +
                "           instance.warning()\n" +
                "           ~~~~~~~~~~~~~~~~~~\n" +
                "2 errors, 2 warnings")
    }

    @Test
    fun `Check method with annotation parameter`() {
        lint().files(
            needInfoAnnotationFile,
            needWarningAnnotationFile,
            needErrorAnnotationFile,
            java("""
                |package com.yy.mobile;
                |
                |import com.yy.mobile.whisper.NeedError;
                |import com.yy.mobile.whisper.NeedInfo;
                |import com.yy.mobile.whisper.NeedWarning;
                |
                |public class ClassTest1 {
                |   
                |   public static void main(String[] args) {
                |       ClassTest1 instance = new ClassTest1();
                |       instance.method1("a");
                |       instance.method2(2);
                |       instance.method3(instance);
                |   }
                |   
                |   public void method1(@NeedInfo("this is method1") String info){}
                |   public void method2(@NeedWarning("this is method2") int info){}
                |   public void method3(@NeedError("this is method3") ClassTest1 info){}
                |}
                |""".trimMargin()),
            kotlin("""
                |package com.yy.mobile
                |
                |import com.yy.mobile.whisper.NeedError
                |import com.yy.mobile.whisper.NeedInfo
                |import com.yy.mobile.whisper.NeedWarning
                |
                |class ClassTest2 {
                |
                |   fun method1(@NeedInfo("this is method1") info: String){}
                |   fun method2(@NeedWarning("this is method2") info: Int){}
                |   fun method3(@NeedError("this is method3") info: ClassTest2){}
                |
                |   companion object {
                |
                |       @JvmStatic
                |       fun main(args: Array<String>) {
                |           val instance = ClassTest2()
                |           instance.method1("a");
                |           instance.method2(2);
                |           instance.method3(instance);
                |       }
                |   }
                |}
            """.trimMargin()))
            .detector(WhisperHintDetector())
            .run()
            .expect("src/com/yy/mobile/ClassTest1.java:13: Error: this is method3 [WhisperError]\n" +
                "       instance.method3(instance);\n" +
                "                        ~~~~~~~~\n" +
                "src/com/yy/mobile/ClassTest2.kt:20: Error: this is method3 [WhisperError]\n" +
                "           instance.method3(instance);\n" +
                "                            ~~~~~~~~\n" +
                "src/com/yy/mobile/ClassTest2.kt:20: Error: this is method3 [WhisperError]\n" +
                "           instance.method3(instance);\n" +
                "                            ~~~~~~~~\n" +
                "src/com/yy/mobile/ClassTest1.java:11: Information: this is method1 [WhisperInfo]\n" +
                "       instance.method1(\"a\");\n" +
                "                        ~~~\n" +
                "src/com/yy/mobile/ClassTest2.kt:18: Information: this is method1 [WhisperInfo]\n" +
                "           instance.method1(\"a\");\n" +
                "                             ~\n" +
                "src/com/yy/mobile/ClassTest2.kt:18: Information: this is method1 [WhisperInfo]\n" +
                "           instance.method1(\"a\");\n" +
                "                             ~\n" +
                "src/com/yy/mobile/ClassTest1.java:12: Warning: this is method2 [WhisperWarning]\n" +
                "       instance.method2(2);\n" +
                "                        ~\n" +
                "src/com/yy/mobile/ClassTest2.kt:19: Warning: this is method2 [WhisperWarning]\n" +
                "           instance.method2(2);\n" +
                "                            ~\n" +
                "src/com/yy/mobile/ClassTest2.kt:19: Warning: this is method2 [WhisperWarning]\n" +
                "           instance.method2(2);\n" +
                "                            ~\n" +
                "3 errors, 3 warnings")
    }
}