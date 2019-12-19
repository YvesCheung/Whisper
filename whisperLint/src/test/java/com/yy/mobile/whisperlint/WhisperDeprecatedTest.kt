package com.yy.mobile.whisperlint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask
import org.junit.Test

/**
 * @author YvesCheung
 * 2018/9/4.
 *
 */
class WhisperDeprecatedTest {

    private val deprecatedAnnotation: TestFile = java("""
                package com.yy.mobile.whisper;

                import java.lang.annotation.Documented;
                import java.lang.annotation.ElementType;
                import java.lang.annotation.Retention;
                import java.lang.annotation.RetentionPolicy;
                import java.lang.annotation.Target;

                @Documented
                @Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
                @Retention(RetentionPolicy.SOURCE)
                public @interface DeprecatedBy {

                    String replaceWith();

                    String message() default "";

                    String receiver() default "";

                    Level level() default Level.Warning;

                    enum Level {
                        Warning, Error
                    }
                }
    """.trimIndent())

    @Test
    fun `Check Java @DeprecatedBy method and new method in the same class`() {
        TestLintTask.lint().files(
            deprecatedAnnotation,
            java("""
                package cc;
                import com.yy.mobile.whisper.DeprecatedBy;

                public class A {

                    public void newMethod() {
                        int b = 3 + 5;
                    }

                    @DeprecatedBy(replaceWith = "newMethod()")
                    public void method() {
                        int a = 3 + 4;
                    }
                }
            """.trimIndent()),
            java("""
                package aa;
                import cc.*;

                public class MainActivity {

                    private A field = new A();

                    MainActivity() {
                        field.method();
                    }
                }
            """.trimIndent()),
            kotlin("""
                package cc

                class B {

                    init {
                        A().apply {
                            method()
                        }
                    }
                }
            """.trimIndent()))
            .detector(WhisperDeprecatedDetector())
            .run()
            .expect("src/cc/B.kt:7: Warning: Use newMethod() instead of this method. [DeprecatedWarning]\n" +
                "            method()\n" +
                "            ~~~~~~~~\n" +
                "src/aa/MainActivity.java:9: Warning: Use field.newMethod() instead of this method. [DeprecatedWarning]\n" +
                "        field.method();\n" +
                "        ~~~~~~~~~~~~~~\n" +
                "0 errors, 2 warnings")
            .expectFixDiffs("Fix for src/cc/B.kt line 7: replace with newMethod():\n" +
                "@@ -7 +7\n" +
                "-             method()\n" +
                "+             newMethod()\n" +
                "Fix for src/aa/MainActivity.java line 9: replace with field.newMethod():\n" +
                "@@ -9 +9\n" +
                "-         field.method();\n" +
                "+         field.newMethod();")
    }

    @Test
    fun `Check Java @DeprecatedBy method with message and new method in the same class`() {
        TestLintTask.lint().files(
            deprecatedAnnotation,
            java("""
                package cc;
                import com.yy.mobile.whisper.DeprecatedBy;

                public class A {

                    public void newMethod() {
                        int b = 3 + 5;
                    }

                    @DeprecatedBy(replaceWith = "newMethod()", message = "哈哈哈" + "hahaha")
                    public void method() {
                        int a = 3 + 4;
                    }
                }
            """.trimIndent()),
            java("""
                package aa;
                import cc.*;

                public class MainActivity {

                    MainActivity() {
                        new A().method();
                    }
                }
            """.trimIndent()))
            .detector(WhisperDeprecatedDetector())
            .run()
            .expect("src/aa/MainActivity.java:7: Warning: 哈哈哈hahaha [DeprecatedWarning]\n" +
                "        new A().method();\n" +
                "        ~~~~~~~~~~~~~~~~\n" +
                "0 errors, 1 warnings")
            .expectFixDiffs("Fix for src/aa/MainActivity.java line 7: replace with new A().newMethod():\n" +
                "@@ -7 +7\n" +
                "-         new A().method();\n" +
                "+         new A().newMethod();")
    }

    @Test
    fun `Check Kotlin @DeprecatedBy method with level error and new method in the same class`() {
        TestLintTask.lint().files(
            deprecatedAnnotation,
            kotlin("""
                package cc;
                import com.yy.mobile.whisper.*

                class A(val a: Int) {

                    fun newMethod() {
                        val b = 3 + 5;
                    }

                    @DeprecatedBy(replaceWith = "newMethod()", level = Level.Error)
                    fun method() {
                        val a = 3 + 4;
                    }
                }
            """.trimIndent()),
            java("""
                package aa;
                import cc.*;

                public class MainActivity {

                    MainActivity() {
                        new A(3).method();
                    }
                }
            """.trimIndent()),
            kotlin("""
                package bb;
                import cc.*;

                internal class B {

                    init {
                        A(4).method()

                        val a = A(3)
                        with(a) {
                            method()
                        }
                    }
                }
            """.trimIndent()))
            .detector(WhisperDeprecatedDetector())
            .run()
            .expect("src/bb/B.kt:7: Error: Use A(4).newMethod() instead of this method. [DeprecatedError]\n" +
                "        A(4).method()\n" +
                "        ~~~~~~~~~~~~~\n" +
                "src/bb/B.kt:11: Error: Use newMethod() instead of this method. [DeprecatedError]\n" +
                "            method()\n" +
                "            ~~~~~~~~\n" +
                "src/aa/MainActivity.java:7: Error: Use new A(3).newMethod() instead of this method. [DeprecatedError]\n" +
                "        new A(3).method();\n" +
                "        ~~~~~~~~~~~~~~~~~\n" +
                "3 errors, 0 warnings")
            .expectFixDiffs("Fix for src/bb/B.kt line 7: replace with A(4).newMethod():\n" +
                "@@ -7 +7\n" +
                "-         A(4).method()\n" +
                "+         A(4).newMethod()\n" +
                "Fix for src/bb/B.kt line 11: replace with newMethod():\n" +
                "@@ -11 +11\n" +
                "-             method()\n" +
                "+             newMethod()\n" +
                "Fix for src/aa/MainActivity.java line 7: replace with new A(3).newMethod():\n" +
                "@@ -7 +7\n" +
                "-         new A(3).method();\n" +
                "+         new A(3).newMethod();")
    }

    @Test
    fun `Check Kotlin @DeprecatedBy method with return type and new method in the same class`() {
        TestLintTask.lint().files(
            deprecatedAnnotation,
            kotlin("""
                package cc;
                import com.yy.mobile.whisper.*

                class A(val a: Int) {

                    fun newMethod():Int {
                        return 3 + 5;
                    }

                    @DeprecatedBy(replaceWith = "newMethod()", message = "use newMethod() instead!!")
                    fun method() = 3 + 4;
                }
            """.trimIndent()),
            java("""
                package aa;
                import cc.*;

                public class MainActivity {

                    MainActivity() {
                        int a = new A(3).method();
                    }
                }
            """.trimIndent()),
            kotlin("""
                package bb;
                import cc.*;

                internal class B {

                    init {
                        val c = A(4).method()

                        val a = A(3)
                        with(a) {
                            val d = method()
                        }

                        val e = a.let { it.method()}
                    }
                }
            """.trimIndent()))
            .detector(WhisperDeprecatedDetector())
            .run()
            .expect("src/bb/B.kt:7: Warning: use newMethod() instead!! [DeprecatedWarning]\n" +
                "        val c = A(4).method()\n" +
                "                ~~~~~~~~~~~~~\n" +
                "src/bb/B.kt:11: Warning: use newMethod() instead!! [DeprecatedWarning]\n" +
                "            val d = method()\n" +
                "                    ~~~~~~~~\n" +
                "src/bb/B.kt:14: Warning: use newMethod() instead!! [DeprecatedWarning]\n" +
                "        val e = a.let { it.method()}\n" +
                "                        ~~~~~~~~~~~\n" +
                "src/aa/MainActivity.java:7: Warning: use newMethod() instead!! [DeprecatedWarning]\n" +
                "        int a = new A(3).method();\n" +
                "                ~~~~~~~~~~~~~~~~~\n" +
                "0 errors, 4 warnings")
            .expectFixDiffs("Fix for src/bb/B.kt line 7: replace with A(4).newMethod():\n" +
                "@@ -7 +7\n" +
                "-         val c = A(4).method()\n" +
                "+         val c = A(4).newMethod()\n" +
                "Fix for src/bb/B.kt line 11: replace with newMethod():\n" +
                "@@ -11 +11\n" +
                "-             val d = method()\n" +
                "+             val d = newMethod()\n" +
                "Fix for src/bb/B.kt line 14: replace with it.newMethod():\n" +
                "@@ -14 +14\n" +
                "-         val e = a.let { it.method()}\n" +
                "+         val e = a.let { it.newMethod()}\n" +
                "Fix for src/aa/MainActivity.java line 7: replace with new A(3).newMethod():\n" +
                "@@ -7 +7\n" +
                "-         int a = new A(3).method();\n" +
                "+         int a = new A(3).newMethod();")
    }

    @Test
    fun `Check Kotlin @DeprecatedBy method and new method in the different class`() {
        TestLintTask.lint().files(
            deprecatedAnnotation,
            kotlin("""
                package cc;
                import com.yy.mobile.whisper.*

                class A(val a: Int) {

                    fun newMethod() {
                        val b = 3 + 5;
                    }

                    @DeprecatedBy(replaceWith = "newMethod()", level = Level.Error, receiver = "new aa.B()")
                    fun method() {
                        val a = 3 + 4;
                    }
                }
            """.trimIndent()),
            java("""
                package aa;

                public class B {

                    public int newMethod() {
                        return 3 + 5;
                    }
                }
            """.trimIndent()),
            kotlin("""
                package bb;
                import cc.*;

                internal class C {

                    init {
                        A(4).method()

                        val a = A(3)
                        with(a) {
                            method()
                        }
                    }
                }
            """.trimIndent()))
            .detector(WhisperDeprecatedDetector())
            .run()
            .expect("src/bb/C.kt:7: Error: Use new aa.B().newMethod() instead of this method. [DeprecatedError]\n" +
                "        A(4).method()\n" +
                "        ~~~~~~~~~~~~~\n" +
                "src/bb/C.kt:11: Error: Use new aa.B().newMethod() instead of this method. [DeprecatedError]\n" +
                "            method()\n" +
                "            ~~~~~~~~\n" +
                "2 errors, 0 warnings")
            .expectFixDiffs("Fix for src/bb/C.kt line 7: replace with new aa.B().newMethod():\n" +
                "@@ -7 +7\n" +
                "-         A(4).method()\n" +
                "+         new aa.B().newMethod()\n" +
                "Fix for src/bb/C.kt line 11: replace with new aa.B().newMethod():\n" +
                "@@ -11 +11\n" +
                "-             method()\n" +
                "+             new aa.B().newMethod()")
    }

    @Test
    fun `Check Java @DeprecatedBy method with arguments and new method in the same class`() {
        TestLintTask.lint().files(
            deprecatedAnnotation,
            java("""
                package cc;
                import com.yy.mobile.whisper.DeprecatedBy;

                public class A {

                    public String newMethod(int a, String b) {
                        return b + String.valueOf(a);
                    }

                    @DeprecatedBy(replaceWith = "newMethod(%1${'$'}s, %3${'$'}s)")
                    public String method(String b, String c, int a) {
                        int a = 3 + 4;
                    }
                }
            """.trimIndent()),
            java("""
                package aa;
                import cc.*;

                public class MainActivity {

                    MainActivity() {
                        int a = new A().method("b", "c", 3);
                    }
                }
            """.trimIndent()),
            kotlin("""
                package bb;
                import cc.*;

                internal class B {

                    init {
                        val c = A().method("b", "c" ,4)

                        val a = A()
                        with(a) {
                            val d = method("bbbbbbbb", "cccccccc", 5)
                        }

                        val e = a.let { it.method("b", "c", 6)}
                    }
                }
            """.trimIndent()))
            .detector(WhisperDeprecatedDetector())
            .run()
            .expect("src/bb/B.kt:7: Warning: Use A().newMethod(\"b\", 4) instead of this method. [DeprecatedWarning]\n" +
                "        val c = A().method(\"b\", \"c\" ,4)\n" +
                "                ~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "src/bb/B.kt:11: Warning: Use newMethod(\"bbbbbbbb\", 5) instead of this method. [DeprecatedWarning]\n" +
                "            val d = method(\"bbbbbbbb\", \"cccccccc\", 5)\n" +
                "                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "src/bb/B.kt:14: Warning: Use it.newMethod(\"b\", 6) instead of this method. [DeprecatedWarning]\n" +
                "        val e = a.let { it.method(\"b\", \"c\", 6)}\n" +
                "                        ~~~~~~~~~~~~~~~~~~~~~~\n" +
                "src/aa/MainActivity.java:7: Warning: Use new A().newMethod(\"b\", 3) instead of this method. [DeprecatedWarning]\n" +
                "        int a = new A().method(\"b\", \"c\", 3);\n" +
                "                ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "0 errors, 4 warnings")
            .expectFixDiffs("Fix for src/bb/B.kt line 7: replace with A().newMethod(\"b\", 4):\n" +
                "@@ -7 +7\n" +
                "-         val c = A().method(\"b\", \"c\" ,4)\n" +
                "+         val c = A().newMethod(\"b\", 4)\n" +
                "Fix for src/bb/B.kt line 11: replace with newMethod(\"bbbbbbbb\", 5):\n" +
                "@@ -11 +11\n" +
                "-             val d = method(\"bbbbbbbb\", \"cccccccc\", 5)\n" +
                "+             val d = newMethod(\"bbbbbbbb\", 5)\n" +
                "Fix for src/bb/B.kt line 14: replace with it.newMethod(\"b\", 6):\n" +
                "@@ -14 +14\n" +
                "-         val e = a.let { it.method(\"b\", \"c\", 6)}\n" +
                "+         val e = a.let { it.newMethod(\"b\", 6)}\n" +
                "Fix for src/aa/MainActivity.java line 7: replace with new A().newMethod(\"b\", 3):\n" +
                "@@ -7 +7\n" +
                "-         int a = new A().method(\"b\", \"c\", 3);\n" +
                "+         int a = new A().newMethod(\"b\", 3);")
    }
}