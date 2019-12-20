package com.yy.mobile.whisperlint.support

import com.android.SdkConstants
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.JavaContext
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UExpression

/**
 * @author YvesCheung
 * 2019-12-20
 *
 * copy from [com.android.tools.lint.detector.api.UastLintUtils]
 * Adapt to a version lower than [CURRENT_API]
 */
object AnnotationCompat {

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
        return AnnotationValuesExtractor
            .getAnnotationValuesExtractor(annotation)
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
        return AnnotationValuesExtractor
            .getAnnotationValuesExtractor(annotation)
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
    fun getAnnotationDoubleValue(
        annotation: UAnnotation?,
        name: String
    ): Double? {
        return AnnotationValuesExtractor
            .getAnnotationValuesExtractor(annotation)
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
        return AnnotationValuesExtractor
            .getAnnotationValuesExtractor(annotation)
            .getAnnotationStringValue(annotation, name)
    }

    @JvmStatic
    fun getAnnotationStringValues(
        annotation: UAnnotation?,
        name: String
    ): Array<String>? {
        return AnnotationValuesExtractor
            .getAnnotationValuesExtractor(annotation)
            .getAnnotationStringValues(annotation, name)
    }
}