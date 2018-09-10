package com.yy.mobile.whisperlint

import com.android.tools.lint.detector.api.getMethodName
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.getOutermostQualified
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.tryResolve

/**
 * Created by 张宇 on 2018/9/8.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
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

fun UExpression.getAvailableReturnReference(): List<UElement> {
    val result = mutableListOf<UElement>()
    val qualified: UExpression? = this.getOutermostQualified()
        ?: this as? UCallExpression
    if (qualified != null) {
        val assignExpect = qualified.getParentOfType<UBinaryExpression>(
            UBinaryExpression::class.java, true)

        if (assignExpect != null) {
            result.add(assignExpect.leftOperand)
        }

        val declareExpect = qualified.getParentOfType<UVariable>(
            UVariable::class.java, true)

        if (declareExpect != null) {
            result.add(declareExpect)
        }
    }
    return result
}