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
            .expect("src/aa/A.kt:9: Error: Must be one of [4, 5] [WhisperIntDef]\n" +
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
            .expect("src/aa/A.kt:9: Error: Must be one of [4, 5] [WhisperIntDef]\n" +
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
            .expect("src/aa/A.kt:10: Error: Must be one of [14, 10] [WhisperIntDef]\n" +
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
            .expect("src/aa/A.kt:9: Error: Must be one of [4, 5] [WhisperIntDef]\n" +
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
            .expect("src/aa/A.kt:10: Error: Must be one of [14, 10] [WhisperIntDef]\n" +
                "        checkInt(2)\n" +
                "                 ~\n" +
                "1 errors, 0 warnings")
    }
}