package com.github.yvescheung.whisperlint.support.api6

import com.android.SdkConstants.ATTR_VALUE
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.ConstantEvaluator
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiArrayInitializerMemberValue
import com.intellij.psi.impl.compiled.ClsAnnotationImpl
import com.github.yvescheung.whisperlint.support.VersionChecker
import com.github.yvescheung.whisperlint.support.api2.attributeNameCompat
import org.jetbrains.kotlin.asJava.elements.KtLightPsiNameValuePair
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.utils.addIfNotNull
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.toUElement
import org.jetbrains.uast.util.isArrayInitializer

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

    internal fun getAnnotationStringValues(annotation: UAnnotation?, name: String): Array<String>? {
        return getAnnotationValues(String::class.java, annotation, name)
            ?.toTypedArray()
            ?.takeIf { it.isNotEmpty() }
    }

    internal fun getAnnotationIntValues(annotation: UAnnotation?, name: String): Array<Int>? {
        return getAnnotationValues(Number::class.java, annotation, name)
            ?.map { it.toInt() }
            ?.toTypedArray()
            ?.takeIf { it.isNotEmpty() }
    }

    internal fun getAnnotationLongValues(annotation: UAnnotation?, name: String): Array<Long>? {
        return getAnnotationValues(Number::class.java, annotation, name)
            ?.map { it.toLong() }
            ?.toTypedArray()
            ?.takeIf { it.isNotEmpty() }
    }

    internal abstract fun <DATA> getAnnotationValues(
        cls: Class<DATA>,
        annotation: UAnnotation?,
        name: String
    ): Collection<DATA>?

    private object Source : AnnotationValuesExtractor() {

        override fun getAnnotationConstantObject(annotation: UAnnotation?, name: String): Any? =
            annotation?.findDeclaredAttributeValue(name)?.let { ConstantEvaluator.evaluate(null, it) }

        override fun <DATA> getAnnotationValues(
            cls: Class<DATA>,
            annotation: UAnnotation?,
            name: String
        ): Collection<DATA>? {

            //fix KotlinUastLanguagePlugin bug.
            //Can't correctly convert @Annotation(literal,literal,literal) to UCallExpression
            val attrValue = mutableListOf<KtValueArgument>()
            val psiAnnotation =
                if (VersionChecker.envVersion() <= 1) {
                    annotation?.psi as? PsiAnnotation ?: return null
                } else {
                    annotation?.javaPsi ?: return null
                }
            for (attr in psiAnnotation.parameterList.attributes) {
                @Suppress("SENSELESS_COMPARISON")
                if ((attr.attributeNameCompat == null && name == ATTR_VALUE) ||
                    attr.attributeNameCompat == name) {
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
                    .filterIsInstance(cls)

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
                    return initializer
                        .map { e -> constantEvaluator.evaluate(e) }
                        .filterIsInstance(cls)

                } else {
                    // Use constant evaluator since we want to resolve field references as well
                    return when (val o = ConstantEvaluator.evaluate(null, attributeValue)) {
                        cls.isInstance(o) -> {
                            @Suppress("UNCHECKED_CAST")
                            listOf(o) as List<DATA>
                        }
                        is Array<*> -> {
                            o.filterIsInstance(cls)
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

        override fun <DATA> getAnnotationValues(
            cls: Class<DATA>,
            annotation: UAnnotation?,
            name: String
        ): Collection<DATA>? {
            val clsAnnotation = getClsAnnotation(annotation)
                ?: return null
            val attribute = clsAnnotation.findDeclaredAttributeValue(name) ?: return null

            if (attribute is PsiArrayInitializerMemberValue) {
                val initializer = attribute.initializers
                val constantEvaluator = ConstantEvaluator()

                return initializer
                    .map { e -> constantEvaluator.evaluate(e) }
                    .filterIsInstance(cls)
            } else {
                // Use constant evaluator since we want to resolve field references as well
                return when (val o = ConstantEvaluator.evaluate(null, attribute)) {
                    cls.isInstance(o) -> {
                        @Suppress("UNCHECKED_CAST")
                        listOf(o) as List<DATA>
                    }
                    is Array<*> -> {
                        o.filterIsInstance(cls)
                    }
                    else -> {
                        null
                    }
                }
            }
        }
    }
}