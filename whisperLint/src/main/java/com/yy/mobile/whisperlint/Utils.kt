package com.yy.mobile.whisperlint

import com.android.tools.lint.detector.api.getMethodName
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
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