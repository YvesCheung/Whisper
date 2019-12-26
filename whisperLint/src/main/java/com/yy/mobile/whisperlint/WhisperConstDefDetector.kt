package com.yy.mobile.whisperlint

import com.android.SdkConstants.ATTR_VALUE
import com.android.tools.lint.detector.api.AnnotationUsageType
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiArrayType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiType
import com.intellij.psi.PsiVariable
import com.yy.mobile.whisperlint.support.api2.AnnotationUsageTypeCompat
import com.yy.mobile.whisperlint.support.api6.AnnotationCompat
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UBlockExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UIfExpression
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UParenthesizedExpression
import org.jetbrains.uast.UPolyadicExpression
import org.jetbrains.uast.UPrefixExpression
import org.jetbrains.uast.UReferenceExpression
import org.jetbrains.uast.UReturnExpression
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.UastBinaryOperator
import org.jetbrains.uast.toUElement
import org.jetbrains.uast.util.isArrayInitializer
import org.jetbrains.uast.util.isNewArrayWithInitializer
import java.util.*

/**
 * @author YvesCheung
 * 2019-12-24
 */
class WhisperConstDefDetector : Detector(), Detector.UastScanner {

    companion object {

        val ISSUE_WHISPER_CONST_DEFINE: Issue = Issue.create(
            "WhisperConstDef",
            "Incorrect constant",
            "Ensures that when parameter in a method only allows a specific set of constants",
            Category.USABILITY,
            1,
            Severity.ERROR,
            Implementation(
                WhisperConstDefDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE)
            ))

        private const val intAnnotation = "$AnnotationPkg.IntDef"
        private const val longAnnotation = "$AnnotationPkg.LongDef"
        private const val stringAnnotation = "$AnnotationPkg.StringDef"

        fun getIssue() = arrayOf(ISSUE_WHISPER_CONST_DEFINE)
    }

    override fun applicableAnnotations() =
        listOf(intAnnotation, longAnnotation, stringAnnotation)

    override fun isApplicableAnnotationUsage(type: AnnotationUsageType): Boolean =
        type in AnnotationUsageTypeCompat.setOf(
            AnnotationUsageTypeCompat.METHOD_CALL_PARAMETER)

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
        val allowValue =
            when (annotation.qualifiedName) {
                intAnnotation -> AnnotationCompat.getAnnotationIntValues(annotation, ATTR_VALUE)
                longAnnotation -> AnnotationCompat.getAnnotationLongValues(annotation, ATTR_VALUE)
                stringAnnotation -> AnnotationCompat.getAnnotationStringValues(annotation, ATTR_VALUE)
                else -> null
            }
        val enumFlag =
            when (annotation.qualifiedName) {
                intAnnotation, longAnnotation -> true
                else -> false
            }
        if (allowValue != null) {
            checkTypeDefConstant(context, annotation, usage, usage, enumFlag, allowValue)
        }
    }

    private fun checkTypeDefConstant(
        context: JavaContext,
        annotation: UAnnotation,
        usage: UElement,
        errorNode: UElement,
        flag: Boolean,
        allowValue: Array<out Any>
    ) {
        if (usage is ULiteralExpression) {
            when (val value = usage.value) {
                null -> // Accepted for @StringDef
                    return
                is String ->
                    assertIncludeValue(
                        context, annotation, usage, errorNode, value, allowValue)
                is Number -> {
                    val v = value.toLong()
                    if (flag && v == 0L) {
                        // Accepted for a flag @IntDef
                        return
                    }
                    assertIncludeValue(
                        context, annotation, usage, errorNode, value, allowValue)
                }
            }
        } else if (AnnotationCompat.isMinusOne(usage)) {
            // -1 is accepted unconditionally for flags
            if (!flag) {
                report(context, annotation, usage, errorNode, allowValue)
            }
        } else if (usage is UPrefixExpression) {
            if (flag) {
                checkTypeDefConstant(
                    context, annotation, usage.operand, errorNode, flag, allowValue)
            } else {
//                val operator = usage.operator
//                if (operator === UastPrefixOperator.BITWISE_NOT) {
//                    report(
//                        context, TypedefDetector.TYPE_DEF, usage, context.getLocation(usage),
//                        "Flag not allowed here"
//                    )
//                } else if (operator === UastPrefixOperator.UNARY_MINUS) {
//                    reportTypeDef(context, annotation, usage, errorNode, allAnnotations)
//                }
            }
        } else if (usage is UParenthesizedExpression) {
            val expression = usage.expression
            checkTypeDefConstant(context, annotation, expression, expression, flag, allowValue)
        } else if (usage is UIfExpression) {
            // If it's ?: then check both the if and else clauses
            val thenExp = usage.thenExpression
            val elseExp = usage.elseExpression
            if (thenExp != null) {
                checkTypeDefConstant(
                    context, annotation, thenExp, errorNode, flag, allowValue)
            }
            if (elseExp != null) {
                checkTypeDefConstant(
                    context, annotation, elseExp, errorNode, flag, allowValue)
            }
        } else if (usage is UPolyadicExpression) {
            val calculateResult = usage.evaluate()
            if (calculateResult != null) {
                if (allowValue.contains(calculateResult)) {
                    return
                }
            }
            if (flag) {
                // Allow &'ing with masks
                if (usage.operator === UastBinaryOperator.BITWISE_AND) {
                    for (operand in usage.operands) {
                        if (operand is UReferenceExpression) {
                            val resolvedName = operand.resolvedName
                            if (resolvedName != null && resolvedName.contains("mask", true)) {
                                return
                            }
                        }
                    }
                }

                for (operand in usage.operands) {
                    checkTypeDefConstant(
                        context, annotation, operand, errorNode, flag, allowValue)
                }
            } else {
//                val operator = usage.operator
//                if (operator === UastBinaryOperator.BITWISE_AND ||
//                    operator === UastBinaryOperator.BITWISE_OR ||
//                    operator === UastBinaryOperator.BITWISE_XOR
//                ) {
//                    report(
//                        context, TypedefDetector.TYPE_DEF, usage, context.getLocation(usage),
//                        "Flag not allowed here"
//                    )
//                }
            }
        } else if (usage is UReferenceExpression) {
            val resolved = usage.resolve()
            if (resolved is PsiVariable) {
                if (resolved.type is PsiArrayType) {
                    // Allow checking the initializer here even if the field itself
                    // isn't final or static; check that the individual values are okay
                    val exp = (resolved.toUElement() as UVariable).uastInitializer
                    if (exp != null) {
                        checkTypeDefConstant(context, annotation, exp, errorNode, flag, allowValue)
                        return
                    }
                }

                // If it's a constant (static/final) check that it's one of the allowed ones
                if (resolved.hasModifierProperty(PsiModifier.STATIC) &&
                    resolved.hasModifierProperty(PsiModifier.FINAL)
                ) {
                    val constant = resolved.computeConstantValue()
                    if (constant != null) {
                        assertIncludeValue(
                            context, annotation, usage, errorNode, constant, allowValue)
                    }
                } else {
                    val lastAssignment = AnnotationCompat.findLastAssignment(resolved, usage)

                    if (lastAssignment != null) {
                        checkTypeDefConstant(
                            context, annotation, lastAssignment, errorNode, flag, allowValue)
                    }
                }
            } else if (resolved is PsiMethod) {
                checkTypeDefConstantForMethodReturn(
                    context, annotation, usage, errorNode, flag, allowValue)
            }
        } else if (usage is UCallExpression) {

            fun UCallExpression.isKotlinArrayFunction(): Boolean {
                val name = this.methodName ?: return false
                return name.contains("arrayOf") || name.contains("ArrayOf")
            }

            if (usage.isNewArrayWithInitializer() ||
                usage.isArrayInitializer() ||
                usage.isKotlinArrayFunction()) {
                var type = usage.getExpressionType()
                if (type != null) {
                    type = type.deepComponentType
                }

                val anyPsi = context.psiFile ?: return
                if (PsiType.INT == type ||
                    PsiType.INT.getBoxedType(anyPsi) == type ||
                    PsiType.LONG == type ||
                    PsiType.LONG.getBoxedType(anyPsi) == type
                ) {
                    for (expression in usage.valueArguments) {
                        checkTypeDefConstant(
                            context, annotation, expression, errorNode, flag, allowValue)
                    }
                }
            } else {
                checkTypeDefConstantForMethodReturn(
                    context, annotation, usage, errorNode, flag, allowValue)
            }
        }
    }

    private fun checkTypeDefConstantForMethodReturn(
        context: JavaContext,
        annotation: UAnnotation,
        usage: UElement,
        errorNode: UElement,
        flag: Boolean,
        allowValue: Array<out Any>
    ) {
        // See if we're passing in a variable which itself has been annotated with
        // a typedef annotation; if so, make sure that the typedef constants are the
        // same, or a subset of the allowed constants
        val resolvedArgument: PsiElement? =
            when (usage) {
                is UReferenceExpression -> usage.resolve()
                is UCallExpression -> usage.resolve()
                else -> null
            }
        if (resolvedArgument is PsiMethod) {
            val fqn = annotation.qualifiedName
            if (fqn != null) {
                val methodAnnotation = resolvedArgument.getAnnotation(fqn)
                    ?.toUElement() as? UAnnotation
                if (methodAnnotation != null) {
                    val subSet =
                        AnnotationCompat.getAnnotationValues(methodAnnotation, ATTR_VALUE)
                    if (subSet != null && subSet.all { it in allowValue }) {
                        return
                    }
                }
            }
            // Called some random method which has not been annotated.
            // Let's peek inside to see if we can figure out more about it; if not,
            // we don't want to flag it since it could get noisy with false
            // positives.
            val uMethod = resolvedArgument.toUElement()
            if (uMethod is UMethod) {
                val body = uMethod.uastBody
                val retValue = if (body is UBlockExpression) {
                    if (body.expressions.size == 1 && body.expressions[0] is UReturnExpression) {
                        val ret = body.expressions[0] as UReturnExpression
                        ret.returnExpression
                    } else {
                        null
                    }
                } else {
                    body
                }
                if (retValue != null) {
                    checkTypeDefConstant(
                        context, annotation, retValue, errorNode, flag, allowValue)
                }
            }
        }
    }

    private fun assertIncludeValue(
        context: JavaContext,
        annotation: UAnnotation,
        usage: UElement,
        errorNode: UElement,
        value: Any,
        allowValue: Array<out Any>
    ) {
        if (!allowValue.contains(value)) {
            report(context, annotation, usage, errorNode, allowValue, value)
        }
    }

    private fun report(
        context: JavaContext,
        annotation: UAnnotation,
        usage: UElement,
        errorNode: UElement,
        allowValue: Array<out Any>,
        value: Any? = null
    ) {
        infix fun UElement.isParentOf(element: UElement): Boolean {
            var p: UElement? = element
            while (p != null) {
                if (p == this) {
                    return true
                }
                p = p.uastParent
            }
            return false
        }

        val location =
            if (errorNode isParentOf usage) {
                context.getLocation(usage)
            } else {
                context.getLocation(usage)
                    .withSecondary(
                        context.getLocation(errorNode),
                        "Here's the @${annotation.qualifiedName} value."
                    )
            }
        var message = "Must be one of ${Arrays.toString(allowValue)}"
        if (value != null) {
            message += ", but actual [$value]"
        }
        context.report(ISSUE_WHISPER_CONST_DEFINE, usage, location, message)
    }
}