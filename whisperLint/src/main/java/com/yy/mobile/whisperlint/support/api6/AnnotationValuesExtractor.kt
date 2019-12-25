package com.yy.mobile.whisperlint.support.api6

import com.android.SdkConstants.ATTR_VALUE
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.ConstantEvaluator
import com.intellij.psi.PsiArrayInitializerMemberValue
import com.intellij.psi.impl.compiled.ClsAnnotationImpl
import com.yy.mobile.whisperlint.support.VersionChecker
import org.jetbrains.kotlin.asJava.elements.KtLightPsiNameValuePair
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.utils.addIfNotNull
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.toUElement
import org.jetbrains.uast.util.isArrayInitializer
import java.util.*

/**
 * copy from [com.android.tools.lint.detector.api.AnnotationValuesExtractor].
 * Adapt to a version lower than [CURRENT_API]
 */
internal sealed class AnnotationValuesExtractor {

    companion object {
        @JvmStatic
        internal fun getAnnotationValuesExtractor(annotation: UAnnotation?): AnnotationValuesExtractor {
            if (VersionChecker.envVersion() <= 1) {
                return Source
            }
            return if (annotation?.javaPsi is ClsAnnotationImpl) Compiled else Source
        }
    }

    internal abstract fun getAnnotationConstantObject(annotation: UAnnotation?, name: String): Any?

    internal fun getAnnotationBooleanValue(annotation: UAnnotation?, name: String): Boolean? =
        getAnnotationConstantObject(annotation, name) as? Boolean

    internal fun getAnnotationLongValue(annotation: UAnnotation?, name: String): Long? =
        (getAnnotationConstantObject(annotation, name) as? Number)?.toLong()

    internal fun getAnnotationDoubleValue(annotation: UAnnotation?, name: String): Double? =
        (getAnnotationConstantObject(annotation, name) as? Number)?.toDouble()

    internal fun getAnnotationStringValue(annotation: UAnnotation?, name: String): String? =
        getAnnotationConstantObject(annotation, name) as? String

    internal fun getAnnotationIntValue(annotation: UAnnotation?, name: String): Int? =
        (getAnnotationConstantObject(annotation, name) as? Number)?.toInt()

    internal abstract fun getAnnotationStringValues(annotation: UAnnotation?, name: String): Array<String>?

    internal abstract fun getAnnotationIntValues(annotation: UAnnotation?, name: String): Array<Int>?

    internal abstract fun getAnnotationLongValues(annotation: UAnnotation?, name: String): Array<Long>?

    private object Source : AnnotationValuesExtractor() {
        override fun getAnnotationConstantObject(annotation: UAnnotation?, name: String): Any? =
            annotation?.findDeclaredAttributeValue(name)?.let { ConstantEvaluator.evaluate(null, it) }

        override fun getAnnotationStringValues(annotation: UAnnotation?, name: String): Array<String>? {
            return getAnnotationValues(annotation, name)
        }

        override fun getAnnotationIntValues(annotation: UAnnotation?, name: String): Array<Int>? {
            return getAnnotationValues(annotation, name)
        }

        override fun getAnnotationLongValues(annotation: UAnnotation?, name: String): Array<Long>? {
            return getAnnotationValues(annotation, name)
        }

        private inline fun <reified DATA> getAnnotationValues(
            annotation: UAnnotation?,
            name: String
        ): Array<DATA>? {
            val psiAnnotation = annotation?.javaPsi ?: return null

            //fix KotlinUastLanguagePlugin bug.
            //Can't correctly convert @Annotation(literal,literal,literal) to UCallExpression
            val attrValue = mutableListOf<KtValueArgument>()
            for (attr in psiAnnotation.attributes) {
                @Suppress("SENSELESS_COMPARISON")
                if ((attr.attributeName == null && name == ATTR_VALUE) ||
                    attr.attributeName == name) {
                    val ktArgument = (attr as? KtLightPsiNameValuePair)?.valueArgument
                    attrValue.addIfNotNull(ktArgument)
                }
            }

            if (attrValue.size > 1) {
                val constantEvaluator = ConstantEvaluator()
                return attrValue
                    .map {
                        constantEvaluator.evaluate(it.getArgumentExpression().toUElement())
                    }
                    .filterIsInstance(DATA::class.java)
                    .toTypedArray()

            } else {
                var attributeValue = annotation.findDeclaredAttributeValue(name)

                if (attributeValue == null && name == ATTR_VALUE) { //default "value" allow anonymous
                    attributeValue = annotation.findAttributeValue(null)
                }

                if (attributeValue == null) {
                    return null
                }

                if (attributeValue.isArrayInitializer()) {
                    val initializer = (attributeValue as UCallExpression).valueArguments

                    val constantEvaluator = ConstantEvaluator()
                    val result = initializer.stream()
                        .map { e -> constantEvaluator.evaluate(e) }
                        .filter { e -> e is DATA }
                        .map { e -> e as DATA }
                        .toArray<DATA> { size -> arrayOfNulls(size) }

                    return result.takeIf { result.isNotEmpty() }?.let { it }
                } else {
                    // Use constant evaluator since we want to resolve field references as well
                    return when (val o = ConstantEvaluator.evaluate(null, attributeValue)) {
                        is DATA -> {
                            arrayOf(o)
                        }
                        is Array<*> -> {
                            Arrays.stream(o)
                                .filter { e -> e is DATA }
                                .map { e -> e as DATA }
                                .toArray<DATA> { size -> arrayOfNulls(size) }
                        }
                        else -> {
                            null
                        }
                    }
                }
            }
        }
    }

    private object Compiled : AnnotationValuesExtractor() {
        private fun getClsAnnotation(annotation: UAnnotation?): ClsAnnotationImpl? =
            (annotation?.javaPsi) as? ClsAnnotationImpl

        override fun getAnnotationConstantObject(annotation: UAnnotation?, name: String): Any? =
            getClsAnnotation(annotation)?.findDeclaredAttributeValue(name)?.let { ConstantEvaluator.evaluate(null, it) }

        override fun getAnnotationStringValues(annotation: UAnnotation?, name: String): Array<String>? {
            return getAnnotationValues(annotation, name)
        }

        override fun getAnnotationIntValues(annotation: UAnnotation?, name: String): Array<Int>? {
            return getAnnotationValues(annotation, name)
        }

        override fun getAnnotationLongValues(annotation: UAnnotation?, name: String): Array<Long>? {
            return getAnnotationValues(annotation, name)
        }

        private inline fun <reified DATA> getAnnotationValues(
            annotation: UAnnotation?,
            name: String
        ): Array<DATA>? {
            val clsAnnotation = getClsAnnotation(annotation)
                ?: return null
            val attribute = clsAnnotation.findDeclaredAttributeValue(name) ?: return null

            if (attribute is PsiArrayInitializerMemberValue) {
                val initializers = attribute.initializers
                val constantEvaluator = ConstantEvaluator()

                val result = Arrays.stream(initializers)
                    .map { e -> constantEvaluator.evaluate(e) }
                    .filter { e -> e is DATA }
                    .map { e -> e as DATA }
                    .toArray<DATA> { size -> arrayOfNulls(size) }

                return result.takeIf { it.isNotEmpty() }?.let { it }
            } else {
                // Use constant evaluator since we want to resolve field references as well
                return when (val o = ConstantEvaluator.evaluate(null, attribute)) {
                    is DATA -> {
                        arrayOf(o)
                    }
                    is Array<*> -> {
                        Arrays.stream(o)
                            .filter { e -> e is DATA }
                            .map { e -> e as DATA }
                            .toArray<DATA> { size -> arrayOfNulls(size) }
                    }
                    else -> {
                        null
                    }
                }
            }
        }
    }
}