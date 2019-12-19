package com.yy.mobile.whisperlint

import com.android.tools.lint.detector.api.LintUtils.getMethodName
import com.intellij.psi.PsiElement
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UField
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.getContainingUVariable
import org.jetbrains.uast.getOutermostQualified
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.tryResolve

/**
 * @author YvesCheung
 * 2018/9/8.
 *
 */
fun UExpression?.maybeIt(): Boolean {
    val receiver = this ?: return false
    val qualifiedParent = receiver.uastParent?.uastParent
    return qualifiedParent !is UQualifiedReferenceExpression &&
        receiver is USimpleNameReferenceExpression &&
        receiver.tryResolve() == null
}

fun UCallExpression.isKotlinReturnSelfFunction(): Boolean {
    val methodName = getMethodName(this)
    return methodName == "apply" ||
        methodName == "also" ||
        methodName == "takeIf" ||
        methodName == "takeUnless"
}

fun UCallExpression.isKotlinScopingFunction(): Boolean {
    val methodName = getMethodName(this)
    return methodName == "apply" ||
        methodName == "run" ||
        methodName == "with" ||
        methodName == "also" ||
        methodName == "let" ||
        methodName == "takeIf" ||
        methodName == "takeUnless"
}

fun UCallExpression.getAvailableCaller(): List<UElement> {
    val result = mutableListOf<UElement>()
    val receiver = this.receiver
    if (receiver != null) {
        result.add(receiver)
    }

    val lambda = this.getParentOfType<ULambdaExpression>(ULambdaExpression::class.java, true)
    (lambda?.uastParent as? UCallExpression)?.let { caller ->
        val lambdaReceiver = caller.receiver
        if (lambdaReceiver != null) {
            result.add(lambdaReceiver)
        }

        val arguments = caller.valueArguments.filter { it !is ULambdaExpression }
        result.addAll(arguments)
    }

    return result
}

fun UExpression.getAvailableReturnValue(): Triple<List<UElement>, List<PsiElement>, List<String>> {
    val result = mutableListOf<UElement>()
    val qualified: UExpression? = this.getOutermostQualified()
        ?: this as? UCallExpression
    if (qualified != null) {
        val assignExpect = qualified.getParentOfType<UBinaryExpression>(
            UBinaryExpression::class.java, true)

        if (assignExpect != null) {
            result.add(assignExpect.leftOperand)
        }

        val declareExpect = qualified.getContainingUVariable()

        if (declareExpect != null) {
            result.add(declareExpect)
        }
    }

    val references = result.mapNotNull { it.tryResolve() }
    val properties = result.mapNotNull { (it as? UField)?.text }
    val instances = mutableListOf(this)

    val methodCall = this.uastParent
    if (methodCall is UQualifiedReferenceExpression &&
        methodCall.selector == this) {
        instances.add(methodCall)
    }

    return Triple(instances, references, properties)
}