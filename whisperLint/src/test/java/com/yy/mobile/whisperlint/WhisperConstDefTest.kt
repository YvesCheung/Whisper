package com.yy.mobile.whisperlint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.java
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
            .expect("src/aa/A.kt:9: Error: Must be one of [4, 5], but actual [3] [WhisperConstDef]\n" +
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
            .expect("src/aa/A.kt:9: Error: Must be one of [4, 5], but actual [3] [WhisperConstDef]\n" +
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
            .expect("src/aa/A.kt:10: Error: Must be one of [14, 10], but actual [2] [WhisperConstDef]\n" +
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
            .expect("src/aa/A.kt:9: Error: Must be one of [4, 5], but actual [3] [WhisperConstDef]\n" +
                "        checkInt(3)\n" +
                "                 ~\n" +
                "1 errors, 0 warnings")
    }

    @Test
    fun `Check intDef in Kotlin function param with const`() {
        lint().files(
            intAnnotation,
            kotlin("""
                package aa
                import com.yy.mobile.whisper.IntDef
                
                class A {
                
                    companion object {
                        const val CONST_FOUR = 4
                        const val CONST_FOUR_TOO = 2 shl 1
                        const val CONST_SIX = 2 * 3
                    }
                
                    fun checkInt(@IntDef(4, 5) param: Int) {}
                
                    fun main(args: String) {
                        checkInt(CONST_FOUR)
                        checkInt(CONST_FOUR_TOO)
                        checkInt(CONST_SIX)
                    }
                }
            """.trimIndent()))
            .detector(WhisperConstDefDetector())
            .run()
            .expect("src/aa/A.kt:17: Error: Must be one of [4, 5], but actual [6] [WhisperConstDef]\n" +
                "        checkInt(CONST_SIX)\n" +
                "                 ~~~~~~~~~\n" +
                "1 errors, 0 warnings")
    }

    @Test
    fun `Check intDef in Kotlin function param with const flag`() {
        lint().files(
            intAnnotation,
            kotlin("""
                package aa
                import com.yy.mobile.whisper.IntDef
                
                class A {
                
                    companion object {
                        const val CONST_FOUR = 4
                        const val CONST_FOUR_TOO = 2 shl 1
                        const val CONST_FIVE = 5
                        const val CONST_SIX = 2 * 3
                    }
                
                    fun checkInt(@IntDef(CONST_FOUR, CONST_FIVE) param: Int) {}
                
                    fun main(args: String) {
                        checkInt(CONST_FOUR or CONST_FIVE)
                        checkInt(CONST_FOUR_TOO and CONST_FOUR)
                        checkInt(CONST_SIX shl CONST_FOUR)
                    }
                }
            """.trimIndent()))
            .detector(WhisperConstDefDetector())
            .run()
            .expect("src/aa/A.kt:18: Error: Must be one of [4, 5], but actual [6] [WhisperConstDef]\n" +
                "        checkInt(CONST_SIX shl CONST_FOUR)\n" +
                "                 ~~~~~~~~~\n" +
                "1 errors, 0 warnings")
    }

    @Test
    fun `Check intDef in Kotlin function param with int vararg`() {
        lint().files(
            intAnnotation,
            kotlin("""
                package aa
                import com.yy.mobile.whisper.IntDef
                
                class A {
                
                    fun checkIntArray(@IntDef(1, 3, 4) vararg param: Int) {}

                    fun main(args: String) {
                        checkIntArray(5, 1, 3, 4, 6)
                    }
                }
            """.trimIndent()))
            .detector(WhisperConstDefDetector())
            .run()
            .expect("src/aa/A.kt:9: Error: Must be one of [1, 3, 4], but actual [5] [WhisperConstDef]\n" +
                "        checkIntArray(5, 1, 3, 4, 6)\n" +
                "                      ~\n" +
                "src/aa/A.kt:9: Error: Must be one of [1, 3, 4], but actual [6] [WhisperConstDef]\n" +
                "        checkIntArray(5, 1, 3, 4, 6)\n" +
                "                                  ~\n" +
                "2 errors, 0 warnings")
    }

    @Test
    fun `Check intDef in Kotlin function param with int array`() {
        lint().files(
            intAnnotation,
            kotlin("""
                package aa
                import com.yy.mobile.whisper.IntDef
                
                class A {

                    fun checkIntArray(@IntDef(1, 3, 4) param: Array<Int>) {}

                    fun main(args: String) {
                        checkIntArray(arrayOf(5, 1, 3, 4, 6))
                        
                        val reference = arrayOf(5, 1, 3, 4, 6)
                        checkIntArray(reference)
                    }
                }
            """.trimIndent()))
            .detector(WhisperConstDefDetector())
            .run()
            .expect("src/aa/A.kt:9: Error: Must be one of [1, 3, 4], but actual [5] [WhisperConstDef]\n" +
                "        checkIntArray(arrayOf(5, 1, 3, 4, 6))\n" +
                "                              ~\n" +
                "src/aa/A.kt:9: Error: Must be one of [1, 3, 4], but actual [6] [WhisperConstDef]\n" +
                "        checkIntArray(arrayOf(5, 1, 3, 4, 6))\n" +
                "                                          ~\n" +
                "src/aa/A.kt:11: Error: Must be one of [1, 3, 4], but actual [5] [WhisperConstDef]\n" +
                "        val reference = arrayOf(5, 1, 3, 4, 6)\n" +
                "                                ~\n" +
                "    src/aa/A.kt:12: Here's the @com.yy.mobile.whisper.IntDef value.\n" +
                "src/aa/A.kt:11: Error: Must be one of [1, 3, 4], but actual [6] [WhisperConstDef]\n" +
                "        val reference = arrayOf(5, 1, 3, 4, 6)\n" +
                "                                            ~\n" +
                "    src/aa/A.kt:12: Here's the @com.yy.mobile.whisper.IntDef value.\n" +
                "4 errors, 0 warnings")
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
                        checkInt(10 + 4)
                        checkInt(10 + 5 + 2)
                    }
                }
            """.trimIndent()))
            .detector(WhisperConstDefDetector())
            .run()
            .expect("src/aa/A.kt:10: Error: Must be one of [14, 10], but actual [2] [WhisperConstDef]\n" +
                "        checkInt(2)\n" +
                "                 ~\n" +
                "src/aa/A.kt:12: Error: Must be one of [14, 10], but actual [2] [WhisperConstDef]\n" +
                "        checkInt(10 + 5 + 2)\n" +
                "                          ~\n" +
                "src/aa/A.kt:12: Error: Must be one of [14, 10], but actual [5] [WhisperConstDef]\n" +
                "        checkInt(10 + 5 + 2)\n" +
                "                      ~\n" +
                "3 errors, 0 warnings")
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
            .expect("src/aa/A.kt:21: Error: Must be one of [4, 5, 10, 12], but actual [13] [WhisperConstDef]\n" +
                "    private fun return13(): Int = 13\n" +
                "                                  ~~\n" +
                "    src/aa/A.kt:11: Here's the @com.yy.mobile.whisper.IntDef value.\n" +
                "1 errors, 0 warnings")
    }

    @Test
    fun `Check intDef in Java function param`() {
        lint().files(
            intAnnotation,
            java("""
                package aa;
                import com.yy.mobile.whisper.IntDef;
                
                public class A {
                
                    private void checkInt(@IntDef({1, 3, 5}) int param) {}
                    
                    private void checkInt2(@IntDef(value = {1, 3, 5}) int param) {}
                    
                    private void checkIntArray(@IntDef({1, 3, 5}) int... param) {}
                    
                    private void checkIntArray2(@IntDef(value = {1, 3, 5}) int... param) {}
                
                    void main(String[] args) {
                        checkInt(3);
                        checkInt(4);
                        checkInt2(5);
                        checkInt2(123121234);
                        checkIntArray(5, 3, 1, 2);
                        checkIntArray(1);
                        checkIntArray2(5, 3, 1, 2);
                        checkIntArray2(1);
                        
                        int reference = 6;
                        checkInt(reference);
                    }
                }
            """.trimIndent()))
            .detector(WhisperConstDefDetector())
            .run()
            .expect("src/aa/A.java:16: Error: Must be one of [1, 3, 5], but actual [4] [WhisperConstDef]\n" +
                "        checkInt(4);\n" +
                "                 ~\n" +
                "src/aa/A.java:18: Error: Must be one of [1, 3, 5], but actual [123121234] [WhisperConstDef]\n" +
                "        checkInt2(123121234);\n" +
                "                  ~~~~~~~~~\n" +
                "src/aa/A.java:19: Error: Must be one of [1, 3, 5], but actual [2] [WhisperConstDef]\n" +
                "        checkIntArray(5, 3, 1, 2);\n" +
                "                               ~\n" +
                "src/aa/A.java:21: Error: Must be one of [1, 3, 5], but actual [2] [WhisperConstDef]\n" +
                "        checkIntArray2(5, 3, 1, 2);\n" +
                "                                ~\n" +
                "src/aa/A.java:24: Error: Must be one of [1, 3, 5], but actual [6] [WhisperConstDef]\n" +
                "        int reference = 6;\n" +
                "                        ~\n" +
                "    src/aa/A.java:25: Here's the @com.yy.mobile.whisper.IntDef value.\n" +
                "5 errors, 0 warnings")
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
            .expect("src/aa/A.kt:10: Error: Must be one of [Hello, world], but actual [Hello world] [WhisperConstDef]\n" +
                "        checkString(\"Hello world\")\n" +
                "                     ~~~~~~~~~~~\n" +
                "1 errors, 0 warnings")
    }

    @Test
    fun `Check stringDef in Kotlin function param with const value`() {
        lint().files(
            stringAnnotation,
            kotlin("""
                package aa
                import com.yy.mobile.whisper.StringDef
                
                class A {
                
                    companion object {
                        const val HELLO = "Hello"
                        const val WORLD = "World"
                    }
                
                    fun checkString(@StringDef(HELLO, WORLD) param: String) {}
                
                    fun main(args: String) {
                        checkString(HELLO)
                        checkString("Hello")
                        checkString("Hello ")
                    }
                }
            """.trimIndent()))
            .detector(WhisperConstDefDetector())
            .run()
            .expect("src/aa/A.kt:16: Error: Must be one of [Hello, World], but actual [Hello ] [WhisperConstDef]\n" +
                "        checkString(\"Hello \")\n" +
                "                     ~~~~~~\n" +
                "1 errors, 0 warnings")
    }
}