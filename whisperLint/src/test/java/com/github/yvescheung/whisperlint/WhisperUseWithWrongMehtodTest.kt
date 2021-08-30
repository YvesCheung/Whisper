package com.github.yvescheung.whisperlint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles
import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.github.yvescheung.whisperlint.WhisperUseWithDetector
import org.junit.Test

/**
 * @author YvesCheung
 * 2018/9/8.
 *
 */
class WhisperUseWithWrongMehtodTest {

    private val useWithAnnotation: TestFile = TestFiles.java("""
                package com.github.yvescheung.whisper;

                import java.lang.annotation.Documented;
                import java.lang.annotation.ElementType;
                import java.lang.annotation.Retention;
                import java.lang.annotation.RetentionPolicy;
                import java.lang.annotation.Target;

                @Documented
                @Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
                @Retention(RetentionPolicy.SOURCE)
                public @interface UseWith {

                    String value();
                }
    """.trimIndent())

    @Test
    fun `Check Kotlin without deInit method`() {
        TestLintTask.lint().files(
            useWithAnnotation,
            kotlin("""
                package cc

                import com.github.yvescheung.whisper.UseWith

                class A {

                    @UseWith("deInit")
                    fun build() {
                    }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("src/cc/A.kt:7: Error: Method [deInit] should be declared in [cc.A] [MissingMethod]\n" +
                "    @UseWith(\"deInit\")\n" +
                "    ^\n" +
                "1 errors, 0 warnings")
    }

    @Test
    fun `Check Kotlin without deInit method with primitive type`() {
        TestLintTask.lint().files(
            useWithAnnotation,
            kotlin("""
                package cc

                import com.github.yvescheung.whisper.UseWith

                class A {

                    @UseWith("deInit")
                    fun build(): Int {
                    }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("src/cc/A.kt:7: Error: Method [deInit] should be declared in [cc.A] [MissingMethod]\n" +
                "    @UseWith(\"deInit\")\n" +
                "    ^\n" +
                "1 errors, 0 warnings")
    }

    @Test
    fun `Check Kotlin without deInit method with custom type`() {
        TestLintTask.lint().files(
            useWithAnnotation,
            kotlin("""
                package cc

                import com.github.yvescheung.whisper.UseWith

                class A {

                    @UseWith("deInit")
                    fun build(): aa.B {
                    }
                }
            """.trimIndent()),
            java("""
                package aa;

                class B {
                    public void haha() { }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("src/cc/A.kt:7: Error: Method [deInit] should be declared in [cc.A] or in [aa.B]. [MissingMethod]\n" +
                "    @UseWith(\"deInit\")\n" +
                "    ^\n" +
                "1 errors, 0 warnings")
    }

    @Test
    fun `Check Kotlin with deInit method in itSelf with custom type`() {
        TestLintTask.lint().files(
            useWithAnnotation,
            kotlin("""
                package cc

                import com.github.yvescheung.whisper.UseWith

                class A {

                    @UseWith("deInit")
                    fun build(): aa.B {
                    }

                    fun deInit(): aa.B {}
                }
            """.trimIndent()),
            java("""
                package aa;

                class B {
                    public void haha() { }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun `Check Kotlin with deInit method in return type with custom type`() {
        TestLintTask.lint().files(
            useWithAnnotation,
            kotlin("""
                package cc

                import com.github.yvescheung.whisper.UseWith

                class A {

                    @UseWith("deInit")
                    fun build(): aa.B {
                    }
                }
            """.trimIndent()),
            java("""
                package aa;

                class B {
                    public void haha() { }

                    public cc.A deInit() { }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun `Check Kotlin with deInit method in itSelf and return type with custom type`() {
        TestLintTask.lint().files(
            useWithAnnotation,
            kotlin("""
                package cc

                import com.github.yvescheung.whisper.UseWith

                class A {

                    @UseWith("deInit")
                    fun build(): aa.B {
                    }

                    fun deInit() {}
                }
            """.trimIndent()),
            java("""
                package aa;

                class B {
                    public void haha() { }

                    public cc.A deInit() { }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun `Check Java without deInit method`() {
        TestLintTask.lint().files(
            useWithAnnotation,
            java("""
                package cc;

                import com.github.yvescheung.whisper.UseWith;

                class A {

                    @UseWith("deInit")
                    public void build() {
                    }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("src/cc/A.java:7: Error: Method [deInit] should be declared in [cc.A] [MissingMethod]\n" +
                "    @UseWith(\"deInit\")\n" +
                "    ^\n" +
                "1 errors, 0 warnings")
    }

    @Test
    fun `Check Java without deInit method with primitive type`() {
        TestLintTask.lint().files(
            useWithAnnotation,
            java("""
                package cc;

                import com.github.yvescheung.whisper.UseWith;

                class A {

                    @UseWith("deInit")
                    public int build() {
                    }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("src/cc/A.java:7: Error: Method [deInit] should be declared in [cc.A] [MissingMethod]\n" +
                "    @UseWith(\"deInit\")\n" +
                "    ^\n" +
                "1 errors, 0 warnings")
    }

    @Test
    fun `Check Java without deInit method with custom type`() {
        TestLintTask.lint().files(
            useWithAnnotation,
            java("""
                package cc;

                import com.github.yvescheung.whisper.UseWith;
                import aa.B;
                public class A {

                    @UseWith("deInit")
                    public B build() {
                    }
                }
            """.trimIndent()),
            kotlin("""
                package aa

                class B {
                    fun aInit(){}
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("src/cc/A.java:7: Error: Method [deInit] should be declared in [cc.A] or in [aa.B]. [MissingMethod]\n" +
                "    @UseWith(\"deInit\")\n" +
                "    ^\n" +
                "1 errors, 0 warnings")
    }

    @Test
    fun `Check Java with deInit method in itSelf with custom type`() {
        TestLintTask.lint().files(
            useWithAnnotation,
            java("""
                package cc;

                import com.github.yvescheung.whisper.UseWith;

                class A {

                    @UseWith("deInit")
                    public aa.B build() {
                    }

                    public aa.B deInit() {}
                }
            """.trimIndent()),
            java("""
                package aa;

                class B {
                    public void haha() { }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun `Check Java with deInit method in return type with custom type`() {
        TestLintTask.lint().files(
            useWithAnnotation,
            java("""
                package cc;

                import com.github.yvescheung.whisper.UseWith;

                class A {

                    @UseWith("deInit")
                    public aa.B build() {
                    }

                    public aa.B deInit() {}
                }
            """.trimIndent()),
            java("""
                package aa;

                class B {
                    public void haha() { }

                    public cc.A deInit() { }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun `Check Java with deInit method in itSelf and return type with custom type`() {
        TestLintTask.lint().files(
            useWithAnnotation,
            java("""
                package cc;

                import com.github.yvescheung.whisper.UseWith;

                class A {

                    @UseWith("deInit")
                    public aa.B build() {
                    }

                    public aa.B deInit() {}
                }
            """.trimIndent()),
            java("""
                package aa;

                class B {
                    public void haha() { }

                    public cc.A deInit() { }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun `Check Kotlin with deInit method of itSelf`() {
        TestLintTask.lint().files(
            useWithAnnotation,
            kotlin("""
                package cc

                import com.github.yvescheung.whisper.UseWith

                class A {

                    @UseWith("build")
                    fun build(): aa.B {
                    }
                }
            """.trimIndent()),
            kotlin("""
                package aa

                import com.github.yvescheung.whisper.UseWith

                class B {

                    fun build()
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("src/cc/A.kt:7: Error: @UseWith can not use the same parameters [build] as the method name. [MissingMethod]\n" +
                "    @UseWith(\"build\")\n" +
                "    ^\n" +
                "1 errors, 0 warnings")
    }

    @Test
    fun `Check Kotlin with deInit method in object`() {
        TestLintTask.lint().files(
            useWithAnnotation,
            kotlin("""
                package cc

                import com.github.yvescheung.whisper.UseWith

                object A {

                    @UseWith("deInit")
                    fun build(): aa.B {
                    }

                    @kotlin.jvm.JvmStatic
                    fun deInit(): aa.B {}
                }
            """.trimIndent()),
            kotlin("""
                package aa

                import com.github.yvescheung.whisper.UseWith

                object B {

                    @UseWith("haha")
                    fun ahah(): cc.A { }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("src/aa/B.kt:7: Error: Method [haha] should be declared in [aa.B] or in [cc.A]. [MissingMethod]\n" +
                "    @UseWith(\"haha\")\n" +
                "    ^\n" +
                "1 errors, 0 warnings")
    }
}