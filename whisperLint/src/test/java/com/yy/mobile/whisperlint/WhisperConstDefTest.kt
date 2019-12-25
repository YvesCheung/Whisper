package com.yy.mobile.whisperlint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

/**
 * @author YvesCheung
 * 2019-12-24
 */
class WhisperConstDefTest {

    private val intAnnotation: TestFile = kotlin("""
            package com.yy.mobile.whisper

            @MustBeDocumented
            @Retention(AnnotationRetention.SOURCE)
            @Target(
                AnnotationTarget.FUNCTION,
                AnnotationTarget.VALUE_PARAMETER,
                AnnotationTarget.FIELD,
                AnnotationTarget.LOCAL_VARIABLE)
            annotation class IntDef(vararg val value: Int)
    """.trimIndent())

    private val stringAnnotation: TestFile = kotlin("""
            package com.yy.mobile.whisper

            @MustBeDocumented
            @Retention(AnnotationRetention.SOURCE)
            @Target(
                AnnotationTarget.FUNCTION,
                AnnotationTarget.VALUE_PARAMETER,
                AnnotationTarget.FIELD,
                AnnotationTarget.LOCAL_VARIABLE)
            annotation class StringDef(vararg val value: String)
    """.trimIndent())

    @Test
    fun `Check intDef in Kotlin function param with named 'value'`() {
        lint().files(
            intAnnotation,
            kotlin("""
                package aa
                import com.yy.mobile.whisper.IntDef
                
                class A {
                
                    fun checkInt(@IntDef(value = [4, 5]) param: Int) {}
                
                    fun main(args: String) {
                        checkInt(3)
                        checkInt(4)
                    }
                }
            """.trimIndent()))
            .detector(WhisperConstDefDetector())
            .run()
            .expect("src/aa/A.kt:9: Error: Must be one of [4, 5] [WhisperConstDef]\n" +
                "        checkInt(3)\n" +
                "                 ~\n" +
                "1 errors, 0 warnings")
    }

    @Test
    fun `Check intDef in Kotlin function param with brackets`() {
        lint().files(
            intAnnotation,
            kotlin("""
                package aa
                import com.yy.mobile.whisper.IntDef
                
                class A {
                
                    fun checkInt(@IntDef([4, 5]) param: Int) {}
                
                    fun main(args: String) {
                        checkInt(3)
                        checkInt(5)
                    }
                }
            """.trimIndent()))
            .detector(WhisperConstDefDetector())
            .run()
            .expect("src/aa/A.kt:9: Error: Must be one of [4, 5] [WhisperConstDef]\n" +
                "        checkInt(3)\n" +
                "                 ~\n" +
                "1 errors, 0 warnings")
    }

    @Test
    fun `Check intDef in Kotlin function param with bracket and binary expression`() {
        lint().files(
            intAnnotation,
            kotlin("""
                package aa
                import com.yy.mobile.whisper.IntDef
                
                class A {
                
                    fun checkInt(@IntDef([4 + 10, 5 + 5]) param: Int) {}
                
                    fun main(args: String) {
                        checkInt(14)
                        checkInt(2)
                    }
                }
            """.trimIndent()))
            .detector(WhisperConstDefDetector())
            .run()
            .expect("src/aa/A.kt:10: Error: Must be one of [14, 10] [WhisperConstDef]\n" +
                "        checkInt(2)\n" +
                "                 ~\n" +
                "1 errors, 0 warnings")
    }

    @Test
    fun `Check intDef in Kotlin function param`() {
        lint().files(
            intAnnotation,
            kotlin("""
                package aa
                import com.yy.mobile.whisper.IntDef
                
                class A {
                
                    fun checkInt(@IntDef(4, 5) param: Int) {}
                
                    fun main(args: String) {
                        checkInt(3)
                        checkInt(4)
                    }
                }
            """.trimIndent()))
            .detector(WhisperConstDefDetector())
            .run()
            .expect("src/aa/A.kt:9: Error: Must be one of [4, 5] [WhisperConstDef]\n" +
                "        checkInt(3)\n" +
                "                 ~\n" +
                "1 errors, 0 warnings")
    }

    @Test
    fun `Check intDef in Kotlin function param with binary expression`() {
        lint().files(
            intAnnotation,
            kotlin("""
                package aa
                import com.yy.mobile.whisper.IntDef
                
                class A {
                
                    fun checkInt(@IntDef(4 + 10, 5 + 5) param: Int) {}
                
                    fun main(args: String) {
                        checkInt(14)
                        checkInt(2)
                    }
                }
            """.trimIndent()))
            .detector(WhisperConstDefDetector())
            .run()
            .expect("src/aa/A.kt:10: Error: Must be one of [14, 10] [WhisperConstDef]\n" +
                "        checkInt(2)\n" +
                "                 ~\n" +
                "1 errors, 0 warnings")
    }

    @Test
    fun `Check intDef in Kotlin function param with call expression`() {
        lint().files(
            intAnnotation,
            kotlin("""
                package aa
                import com.yy.mobile.whisper.IntDef
                
                class A {
                
                    fun checkInt(@IntDef(4, 5, 10, 12) param: Int) {}
                
                    fun main(args: String) {
                        checkInt(return4())
                        checkInt(return12())
                        checkInt(return13())
                    }
                    
                    @IntDef(4, 5)
                    private fun return4(): Int {
                        return 6
                    }
                    
                    private fun return12(): Int = 12
                    
                    private fun return13(): Int = 13
                }
            """.trimIndent()))
            .detector(WhisperConstDefDetector())
            .run()
            .expect("src/aa/A.kt:11: Error: Must be one of [4, 5, 10, 12] [WhisperConstDef]\n" +
                "        checkInt(return13())\n" +
                "                 ~~~~~~~~~~\n" +
                "    src/aa/A.kt:21: the actual value.\n" +
                "1 errors, 0 warnings")
    }

    @Test
    fun `Check stringDef in Kotlin function param with named 'value'`() {
        lint().files(
            stringAnnotation,
            kotlin("""
                package aa
                import com.yy.mobile.whisper.StringDef
                
                class A {
                
                    fun checkString(@StringDef(value = ["Hello", "world"]) param: String) {}
                
                    fun main(args: String) {
                        checkString("Hello")
                        checkString("Hello world")
                    }
                }
            """.trimIndent()))
            .detector(WhisperConstDefDetector())
            .run()
            .expect("src/aa/A.kt:10: Error: Must be one of [Hello, world] [WhisperConstDef]\n" +
                "        checkString(\"Hello world\")\n" +
                "                     ~~~~~~~~~~~\n" +
                "1 errors, 0 warnings")
    }
}