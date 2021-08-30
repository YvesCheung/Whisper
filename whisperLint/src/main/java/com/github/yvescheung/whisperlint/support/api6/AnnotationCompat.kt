package com.github.yvescheung.whisperlint.support.api6

import com.android.SdkConstants
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.ConstantEvaluator
import com.android.tools.lint.detector.api.JavaContext
import com.intellij.psi.PsiLocalVariable
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiVariable
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.UUnaryExpression
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.UastPrefixOperator
import org.jetbrains.uast.getContainingUMethod
import org.jetbrains.uast.getUastContext

/**
 * compat android gradle plugin < 3.6+
 *
 * copy from [com.android.tools.lint.detector.api.UastLintUtils]
 * Adapt to a version lower than [CURRENT_API]
 *
 * @author YvesCheung
 * 2019-12-20
 */
object AnnotationCompat {

    @JvmStatic
    fun findLastAssignment(
        variable: PsiVariable,
        call: UElement
    ): UExpression? {
        var currVariable = variable
        var lastAssignment: UElement? = null

        if (currVariable is UVariable) {
            currVariable = currVariable.psi
        }

        if (!currVariable.hasModifierProperty(PsiModifier.FINAL) && (currVariable is PsiLocalVariable || currVariable is PsiParameter)) {
            val containingFunction = call.getContainingUMethod()
            if (containingFunction != null) {
                val context = call.getUastContext()
                val finder = ConstantEvaluator.LastAssignmentFinder(
                    currVariable, call, context, null, -1)
                containingFunction.accept(finder)
                lastAssignment = finder.lastAssignment
            }
        } else {
            val context = call.getUastContext()
            lastAssignment = context.getInitializerBody(currVariable)
        }

        return if (lastAssignment is UExpression) lastAssignment
        else null
    }

    @JvmStatic
    fun isMinusOne(argument: UElement): Boolean {
        return if (argument is UUnaryExpression) {
            val operand = argument.operand
            if (operand is ULiteralExpression && argument.operator === UastPrefixOperator.UNARY_MINUS) {
                val value = operand.value
                value is Number && value.toInt() == 1
            } else {
                false
            }
        } else {
            false
        }
    }

    @JvmStatic
    fun getAnnotationValue(annotation: UAnnotation): UExpression? {
        return annotation.findDeclaredAttributeValue(SdkConstants.ATTR_VALUE)
    }

    @JvmStatic
    fun getLongAttribute(
        context: JavaContext,
        annotation: UAnnotation,
        name: String,
        defaultValue: Long
    ): Long {
        return getAnnotationLongValue(annotation, name, defaultValue)
    }

    @JvmStatic
    fun getDoubleAttribute(
        context: JavaContext,
        annotation: UAnnotation,
        name: String,
        defaultValue: Double
    ): Double {
        return getAnnotationDoubleValue(annotation, name, defaultValue)
    }

    @JvmStatic
    fun getBoolean(
        context: JavaContext,
        annotation: UAnnotation,
        name: String,
        defaultValue: Boolean
    ): Boolean {
        return getAnnotationBooleanValue(annotation, name, defaultValue)
    }

    @JvmStatic
    fun getAnnotationBooleanValue(
        annotation: UAnnotation?,
        name: String
    ): Boolean? {
        return AnnotationValuesExtractor.getAnnotationValuesExtractor(annotation)
            .getAnnotationBooleanValue(annotation, name)
    }

    @JvmStatic
    fun getAnnotationBooleanValue(
        annotation: UAnnotation?,
        name: String,
        defaultValue: Boolean
    ): Boolean {
        val value = getAnnotationBooleanValue(annotation, name)
        return value ?: defaultValue
    }

    @JvmStatic
    fun getAnnotationLongValue(
        annotation: UAnnotation?,
        name: String
    ): Long? {
        return AnnotationValuesExtractor.getAnnotationValuesExtractor(annotation)
            .getAnnotationLongValue(annotation, name)
    }

    @JvmStatic
    fun getAnnotationLongValue(
        annotation: UAnnotation?,
        name: String,
        defaultValue: Long
    ): Long {
        val value = getAnnotationLongValue(annotation, name)
        return value ?: defaultValue
    }

    @JvmStatic
    fun getAnnotationLongValues(
        annotation: UAnnotation?,
        name: String
    ): Array<Long>? {
        return AnnotationValuesExtractor.getAnnotationValuesExtractor(annotation)
            .getAnnotationLongValues(annotation, name)
    }

    @JvmStatic
    fun getAnnotationDoubleValue(
        annotation: UAnnotation?,
        name: String
    ): Double? {
        return AnnotationValuesExtractor.getAnnotationValuesExtractor(annotation)
            .getAnnotationDoubleValue(annotation, name)
    }

    @JvmStatic
    fun getAnnotationDoubleValue(
        annotation: UAnnotation?,
        name: String,
        defaultValue: Double
    ): Double {
        val value = getAnnotationDoubleValue(annotation, name)
        return value ?: defaultValue
    }

    @JvmStatic
    fun getAnnotationStringValue(
        annotation: UAnnotation?,
        name: String
    ): String? {
        return AnnotationValuesExtractor.getAnnotationValuesExtractor(annotation)
            .getAnnotationStringValue(annotation, name)
    }

    @JvmStatic
    fun getAnnotationStringValues(
        annotation: UAnnotation?,
        name: String
    ): Array<String>? {
        return AnnotationValuesExtractor.getAnnotationValuesExtractor(annotation)
            .getAnnotationStringValues(annotation, name)
    }

    @JvmStatic
    fun getAnnotationIntValues(
        annotation: UAnnotation?,
        name: String
    ): Array<Int>? {
        return AnnotationValuesExtractor.getAnnotationValuesExtractor(annotation)
            .getAnnotationIntValues(annotation, name)
    }

    @JvmStatic
    fun getAnnotationValues(
        annotation: UAnnotation?,
        name: String
    ): Collection<Any>? {
        return AnnotationValuesExtractor.getAnnotationValuesExtractor(annotation)
            .getAnnotationValues(Any::class.java, annotation, name)
    }
}