package com.github.yvescheung.whisperlint

import com.android.tools.lint.checks.AbstractAnnotationDetector
import com.android.tools.lint.checks.AnnotationDetector.ATTR_FROM
import com.android.tools.lint.checks.AnnotationDetector.ATTR_FROM_INCLUSIVE
import com.android.tools.lint.checks.AnnotationDetector.ATTR_TO
import com.android.tools.lint.checks.AnnotationDetector.ATTR_TO_INCLUSIVE
import com.android.tools.lint.checks.RangeConstraint
import com.android.tools.lint.detector.api.AnnotationUsageType
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.ConstantEvaluator
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.google.common.annotations.VisibleForTesting
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifierListOwner
import com.github.yvescheung.whisperlint.support.api6.AnnotationCompat
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UIfExpression
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.UResolvable
import org.jetbrains.uast.util.isNewArrayWithInitializer

/**
 * @author YvesCheung
 * 2020/12/3
 */
@Suppress("UnstableApiUsage")
class WhisperRangeDetector : AbstractAnnotationDetector(), SourceCodeScanner {

    override fun applicableAnnotations(): List<String> = listOf(
        INT_RANGE_ANNOTATION,
        FLOAT_RANGE_ANNOTATION
    )

    override fun visitAnnotationUsage(
        context: JavaContext,
        usage: UElement,
        type: AnnotationUsageType,
        annotation: UAnnotation,
        qualifiedName: String,
        method: PsiMethod?,
        referenced: PsiElement?,
        annotations: List<UAnnotation>,
        allMemberAnnotations: List<UAnnotation>,
        allClassAnnotations: List<UAnnotation>,
        allPackageAnnotations: List<UAnnotation>
    ) {
        if (qualifiedName == INT_RANGE_ANNOTATION) {
            checkIntRange(context, annotation, usage, annotations)
        } else if (qualifiedName == FLOAT_RANGE_ANNOTATION) {
            checkFloatRange(context, annotation, usage)
        }
    }

    private fun checkIntRange(
        context: JavaContext,
        annotation: UAnnotation,
        argument: UElement,
        allAnnotations: List<UAnnotation>
    ) {
        if (argument is UIfExpression) {
            if (argument.thenExpression != null) {
                checkIntRange(context, annotation, argument.thenExpression!!, allAnnotations)
            }
            if (argument.elseExpression != null) {
                checkIntRange(context, annotation, argument.elseExpression!!, allAnnotations)
            }
            return
        }

        val message = getIntRangeError(context, annotation, argument)
        if (message != null) {
            report(context, RANGE, argument, context.getLocation(argument), message)
        }
    }

    private fun checkFloatRange(
        context: JavaContext,
        annotation: UAnnotation,
        argument: UElement
    ) {
        if (argument is UIfExpression) {
            if (argument.thenExpression != null) {
                checkFloatRange(context, annotation, argument.thenExpression!!)
            }
            if (argument.elseExpression != null) {
                checkFloatRange(context, annotation, argument.elseExpression!!)
            }
            return
        }

        val constraint = FloatRangeConstraint.create(annotation)

        val constant = ConstantEvaluator.evaluate(context, argument)
        if (constant !is Number) {
            // Number arrays
            if (constant is FloatArray ||
                constant is DoubleArray ||
                constant is IntArray ||
                constant is LongArray
            ) {
                if (constant is FloatArray) {
                    for (value in constant) {
                        if (!constraint.isValid(value.toDouble())) {
                            val message = constraint.describe(value.toDouble())
                            report(
                                context, RANGE, argument, context.getLocation(argument),
                                message
                            )
                            return
                        }
                    }
                }
                // Kinda repetitive but primitive arrays are not related by subtyping
                if (constant is DoubleArray) {
                    for (value in constant) {
                        if (!constraint.isValid(value)) {
                            val message = constraint.describe(value)
                            report(
                                context, RANGE, argument, context.getLocation(argument),
                                message
                            )
                            return
                        }
                    }
                }
                if (constant is IntArray) {
                    for (value in constant) {
                        if (!constraint.isValid(value.toDouble())) {
                            val message = constraint.describe(value.toDouble())
                            report(
                                context, RANGE, argument, context.getLocation(argument),
                                message
                            )
                            return
                        }
                    }
                }
                if (constant is LongArray) {
                    for (value in constant) {
                        if (!constraint.isValid(value.toDouble())) {
                            val message = constraint.describe(value.toDouble())
                            report(
                                context, RANGE, argument, context.getLocation(argument),
                                message
                            )
                            return
                        }
                    }
                }
            }

            // Try to resolve it; see if there's an annotation on the variable/parameter/field
            if (argument is UResolvable) {
                val resolved = (argument as UResolvable).resolve()
                // TODO: What about parameters or local variables here?
                // UAST-wise we could look for UDeclaration but it turns out
                // UDeclaration also extends PsiModifierListOwner!
                if (resolved is PsiModifierListOwner) {
                    val referenceConstraint =
                        RangeConstraint.create(resolved, context.evaluator)
                    val here = RangeConstraint.create(annotation)
                    if (here != null && referenceConstraint != null) {
                        val contains = here.contains(referenceConstraint)
                        if (contains != null && !contains) {
                            val message = here.toString()
                            report(context, RANGE, argument, context.getLocation(argument), message)
                        }
                    }
                }
            }

            return
        }

        val value = constant.toDouble()
        if (!constraint.isValid(value)) {
            val message = constraint.describe(
                argument as? UExpression, value
            )
            report(context, RANGE, argument, context.getLocation(argument), message)
        }
    }

    internal class FloatRangeConstraint @VisibleForTesting constructor(val from: Double, val to: Double, val fromInclusive: Boolean, val toInclusive: Boolean) : RangeConstraint() {
        fun isValid(value: Double): Boolean {
            return ((fromInclusive && value >= from || !fromInclusive && value > from)
                && (toInclusive && value <= to || !toInclusive && value < to))
        }

        fun describe(argument: Double): String {
            return describe(null, argument)
        }

        @JvmOverloads
        fun describe(argument: UExpression? = null, actualValue: Double? = null): String {
            val sb = StringBuilder(20)
            var valueString: String? = null
            if (argument is ULiteralExpression) {
                // Use source text instead to avoid rounding errors involved in conversion, e.g
                //    Error: Value must be > 2.5 (was 2.490000009536743) [Range]
                //    printAtLeastExclusive(2.49f); // ERROR
                //                          ~~~~~
                var str = argument.asSourceString()
                if (str.endsWith("f") || str.endsWith("F")) {
                    str = str.substring(0, str.length - 1)
                }
                valueString = str
            } else if (actualValue != null) {
                valueString = actualValue.toString()
            }

            // If we have an actual value, don't describe the full range, only describe
            // the parts that are outside the range
            if (actualValue != null && !isValid(actualValue)) {
                val value: Double = actualValue
                if (from != Double.NEGATIVE_INFINITY) {
                    if (to != Double.POSITIVE_INFINITY) {
                        if (fromInclusive && value < from || !fromInclusive && value <= from) {
                            sb.append("Value must be ")
                            if (fromInclusive) {
                                sb.append('\u2265') // >= sign
                            } else {
                                sb.append('>')
                            }
                            sb.append(' ')
                            sb.append(from.toString())
                        } else {
                            assert(toInclusive && value > to || !toInclusive && value >= to)
                            sb.append("Value must be ")
                            if (toInclusive) {
                                sb.append('\u2264') // <= sign
                            } else {
                                sb.append('<')
                            }
                            sb.append(' ')
                            sb.append(to.toString())
                        }
                    } else {
                        sb.append("Value must be ")
                        if (fromInclusive) {
                            sb.append('\u2265') // >= sign
                        } else {
                            sb.append('>')
                        }
                        sb.append(' ')
                        sb.append(from.toString())
                    }
                } else if (to != Double.POSITIVE_INFINITY) {
                    sb.append("Value must be ")
                    if (toInclusive) {
                        sb.append('\u2264') // <= sign
                    } else {
                        sb.append('<')
                    }
                    sb.append(' ')
                    sb.append(to.toString())
                }
                sb.append(" (was ").append(valueString).append(")")
                return sb.toString()
            }
            if (from != Double.NEGATIVE_INFINITY) {
                if (to != Double.POSITIVE_INFINITY) {
                    sb.append("Value must be ")
                    if (fromInclusive) {
                        sb.append('\u2265') // >= sign
                    } else {
                        sb.append('>')
                    }
                    sb.append(' ')
                    sb.append(from.toString())
                    sb.append(" and ")
                    if (toInclusive) {
                        sb.append('\u2264') // <= sign
                    } else {
                        sb.append('<')
                    }
                    sb.append(' ')
                    sb.append(to.toString())
                } else {
                    sb.append("Value must be ")
                    if (fromInclusive) {
                        sb.append('\u2265') // >= sign
                    } else {
                        sb.append('>')
                    }
                    sb.append(' ')
                    sb.append(from.toString())
                }
            } else if (to != Double.POSITIVE_INFINITY) {
                sb.append("Value must be ")
                if (toInclusive) {
                    sb.append('\u2264') // <= sign
                } else {
                    sb.append('<')
                }
                sb.append(' ')
                sb.append(to.toString())
            }
            if (valueString != null) {
                sb.append(" (is ").append(valueString).append(')')
            }
            return sb.toString()
        }

        override fun contains(other: RangeConstraint): Boolean? {
            if (other is FloatRangeConstraint) {
                return (!(other.from < from || other.to > to)
                    && !(!fromInclusive && other.fromInclusive && other.from == from)
                    && !(!toInclusive && other.toInclusive && other.to == to))
            } else if (other is IntRangeConstraint) {
                return (!(other.from < from || other.to > to)
                    && !(!fromInclusive && other.from.toDouble() == from)
                    && !(!toInclusive && other.to.toDouble() == to))
            }
            return null
        }

        override fun toString(): String {
            return describe(null, null)
        }

        companion object {
            fun create(annotation: UAnnotation): FloatRangeConstraint {
                val from = AnnotationCompat.getAnnotationDoubleValue(
                    annotation, ATTR_FROM, Float.NEGATIVE_INFINITY.toDouble())
                val to = AnnotationCompat.getAnnotationDoubleValue(
                    annotation, ATTR_TO, Float.POSITIVE_INFINITY.toDouble())
                val fromInclusive = AnnotationCompat.getAnnotationBooleanValue(annotation, ATTR_FROM_INCLUSIVE, true)
                val toInclusive = AnnotationCompat.getAnnotationBooleanValue(annotation, ATTR_TO_INCLUSIVE, true)
                return FloatRangeConstraint(from, to, fromInclusive, toInclusive)
            }
        }
    }

    internal class IntRangeConstraint private constructor(val from: Long, val to: Long) : RangeConstraint() {
        fun isValid(value: Long): Boolean {
            return value in from..to
        }

        fun describe(): String {
            return desc(null)
        }

        fun describe(argument: Long): String {
            return desc(argument)
        }

        private fun desc(actualValue: Long?): String {
            val sb = java.lang.StringBuilder(20)

            // If we have an actual value, don't describe the full range, only describe
            // the parts that are outside the range
            if (actualValue != null && !isValid(actualValue)) {
                val value: Long = actualValue
                if (value < from) {
                    sb.append("Value must be \u2265 ")
                    sb.append(from.toString())
                } else {
                    assert(value > to)
                    sb.append("Value must be \u2264 ")
                    sb.append(to.toString())
                }
                sb.append(" (was ").append(value).append(')')
                return sb.toString()
            }
            if (to == Long.MAX_VALUE) {
                sb.append("Value must be \u2265 ")
                sb.append(from.toString())
            } else if (from == Long.MIN_VALUE) {
                sb.append("Value must be \u2264 ")
                sb.append(to.toString())
            } else {
                sb.append("Value must be \u2265 ")
                sb.append(from.toString())
                sb.append(" and \u2264 ")
                sb.append(to.toString())
            }
            if (actualValue != null) {
                sb.append(" (is ").append(actualValue).append(')')
            }
            return sb.toString()
        }

        override fun toString(): String {
            return desc(null)
        }

        override fun contains(other: RangeConstraint): Boolean? {
            if (other is IntRangeConstraint) {
                return other.from >= from && other.to <= to
            } else if (other is FloatRangeConstraint) {
                if (!other.fromInclusive && other.from == from.toDouble()
                    || !other.toInclusive && other.to == to.toDouble()) {
                    return false
                }

                // Both represent infinity
                if (other.to > to && !(java.lang.Double.isInfinite(other.to) && to == Long.MAX_VALUE)) {
                    return false
                }
                return !(other.from < from
                    && !(java.lang.Double.isInfinite(other.from) && from == Long.MIN_VALUE))
            }
            return null
        }

        companion object {
            fun create(annotation: UAnnotation): IntRangeConstraint {
                val from = AnnotationCompat.getAnnotationLongValue(
                    annotation, ATTR_FROM, Int.MIN_VALUE.toLong())
                val to = AnnotationCompat.getAnnotationLongValue(
                    annotation, ATTR_TO, Int.MAX_VALUE.toLong())
                return IntRangeConstraint(from, to)
            }
        }
    }

    companion object {

        private const val INT_RANGE_ANNOTATION = "$AnnotationPkg.IntRange"
        private const val FLOAT_RANGE_ANNOTATION = "$AnnotationPkg.FloatRange"

        fun getIntRangeError(
            context: JavaContext,
            annotation: UAnnotation,
            argument: UElement
        ): String? {
            if (argument.isNewArrayWithInitializer()) {
                val newExpression = argument as UCallExpression
                for (expression in newExpression.valueArguments) {
                    val error = getIntRangeError(context, annotation, expression)
                    if (error != null) {
                        return error
                    }
                }
            }

            val constraint = IntRangeConstraint.create(annotation)

            val o = ConstantEvaluator.evaluate(context, argument)
            if (o !is Number) {
                // Number arrays
                if (o is IntArray || o is LongArray) {
                    if (o is IntArray) {
                        for (value in o) {
                            if (!constraint.isValid(value.toLong())) {
                                return constraint.describe(value.toLong())
                            }
                        }
                    }
                    if (o is LongArray) {
                        for (value in o) {
                            if (!constraint.isValid(value)) {
                                return constraint.describe(value)
                            }
                        }
                    }
                }

                // Try to resolve it; see if there's an annotation on the variable/parameter/field
                if (argument is UResolvable) {
                    val resolved = (argument as UResolvable).resolve()
                    // TODO: What about parameters or local variables here?
                    // UAST-wise we could look for UDeclaration but it turns out
                    // UDeclaration also extends PsiModifierListOwner!
                    if (resolved is PsiModifierListOwner) {
                        val referenceConstraint = RangeConstraint.create(
                            resolved,
                            context.evaluator
                        )
                        val here = RangeConstraint.create(annotation)
                        if (here != null && referenceConstraint != null) {
                            val contains = here.contains(referenceConstraint)
                            if (contains != null && !contains) {
                                return here.toString()
                            }
                        }
                    }
                }

                return null
            }

            val value = o.toLong()
            return if (!constraint.isValid(value)) {
                constraint.describe(value)
            } else null
        }

        /** Makes sure values are within the allowed range */
        @JvmField
        val RANGE = Issue.create(
            id = "WhisperRange",
            briefDescription = "Outside Range",
            explanation = """
                Some parameters are required to in a particular numerical range; this check \
                makes sure that arguments passed fall within the range. For arrays, Strings \
                and collections this refers to the size or length.""",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(
                WhisperRangeDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        fun getIssue() = arrayOf(RANGE)
    }
}