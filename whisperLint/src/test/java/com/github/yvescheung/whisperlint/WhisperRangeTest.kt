package com.github.yvescheung.whisperlint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.github.yvescheung.whisperlint.WhisperRangeDetector
import org.junit.Test

/**
 * @author YvesCheung
 * 2020/12/3
 */
@Suppress("UnstableApiUsage")
class WhisperRangeTest {

    private val intRangeAnnotation = kotlin("""
                package com.github.yvescheung.whisper
                
                import kotlin.annotation.AnnotationRetention.SOURCE
                import kotlin.annotation.AnnotationTarget.FIELD
                import kotlin.annotation.AnnotationTarget.FUNCTION
                import kotlin.annotation.AnnotationTarget.LOCAL_VARIABLE
                import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
                
                @MustBeDocumented
                @Retention(SOURCE)
                @Target(FUNCTION, VALUE_PARAMETER, FIELD, LOCAL_VARIABLE)
                annotation class IntRange(val from: Int = Int.MIN_VALUE, val to: Int = Int.MAX_VALUE)
    """.trimIndent())

    private val floatRangeAnnotation = kotlin("""
                package com.github.yvescheung.whisper
        
                @MustBeDocumented
                @Retention(SOURCE)
                @Target(FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, VALUE_PARAMETER, FIELD, LOCAL_VARIABLE, ANNOTATION_CLASS)
                annotation class FloatRange(
                    val from: Float = java.lang.Float.NEGATIVE_INFINITY,
                    val to: Float = java.lang.Float.POSITIVE_INFINITY,
                    val fromInclusive: Boolean = true,
                    val toInclusive: Boolean = true
                )
    """.trimIndent())

    @Test
    fun `Check IntRange parameters in function`() {
        lint().files(
            intRangeAnnotation,
            floatRangeAnnotation,
            kotlin("""
                package a;
                import com.github.yvescheung.whisper.FloatRange
                import com.github.yvescheung.whisper.IntRange
                
                class Demo {
                    fun methodA(
                        @IntRange(from = -3, to = 3) p1: Int,
                        @FloatRange(to = 100f) p2: Float
                    ) {
                        methodB(p1.toFloat(), p2.toInt())
                    }
                
                    fun methodB(
                        @IntRange(from = 100) p1: Float,
                        @FloatRange(from = 300f, to = 400f) p2: Int
                    ) {
                        methodA(p1.toInt(), p2.toFloat())
                    }
                }
            """.trimIndent()),
            kotlin("""
                package a
                
                class B {
                    val field1 = 4
                    val field2 = 200f
                    
                    init {
                        with(Demo()) {
                            methodA(1, -1000000f) //ok
                            methodA(-100, 0f) //p1 not ok
                            methodA(field1, 0f) //p1 not ok
                            methodA(-3, field2) //p2 not ok
                            methodA(3, 101f) //p2 not ok
                
                            val variable = 0f
                            methodB(200f, 301) //ok
                            methodB(50f + 2, 400) //p1 not ok
                            methodB(variable, 300 + 50) //p1 not ok
                            methodB(variable + 100, -1000000 * 2) //p2 not ok
                            methodB(100f, 1000000) //p2 not ok
                        }
                    }
                }
            """.trimIndent()))
            .detector(WhisperRangeDetector())
            .run()
            .expect("""
                src/a/B.kt:10: Error: Value must be ≥ -3 (was -100) [WhisperRange]
                            methodA(-100, 0f) //p1 not ok
                                    ~~~~
                src/a/B.kt:11: Error: Value must be ≤ 3 (was 4) [WhisperRange]
                            methodA(field1, 0f) //p1 not ok
                                    ~~~~~~
                src/a/B.kt:12: Error: Value must be ≤ 100.0 (was 200.0) [WhisperRange]
                            methodA(-3, field2) //p2 not ok
                                        ~~~~~~
                src/a/B.kt:13: Error: Value must be ≤ 100.0 (was 101.0) [WhisperRange]
                            methodA(3, 101f) //p2 not ok
                                       ~~~~
                src/a/B.kt:17: Error: Value must be ≥ 100 (was 52) [WhisperRange]
                            methodB(50f + 2, 400) //p1 not ok
                                    ~~~~~~~
                src/a/B.kt:18: Error: Value must be ≥ 100 (was 0) [WhisperRange]
                            methodB(variable, 300 + 50) //p1 not ok
                                    ~~~~~~~~
                src/a/B.kt:19: Error: Value must be ≥ 300.0 (was -2000000.0) [WhisperRange]
                            methodB(variable + 100, -1000000 * 2) //p2 not ok
                                                    ~~~~~~~~~~~~
                src/a/B.kt:20: Error: Value must be ≤ 400.0 (was 1000000) [WhisperRange]
                            methodB(100f, 1000000) //p2 not ok
                                          ~~~~~~~
                8 errors, 0 warnings
            """.trimIndent())
    }
}