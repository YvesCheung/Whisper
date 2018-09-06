package com.yy.mobile.whisperlint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.AnnotationUsageType
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.asRecursiveLogString
import org.jetbrains.uast.evaluateString
import org.jetbrains.uast.getContainingUClass
import org.jetbrains.uast.getContainingUFile
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.tryResolve
import java.util.*

/**
 * Created by 张宇 on 2018/9/4.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
class WhisperUseWithDetector : Detector(), Detector.UastScanner {

    companion object {

        val ISSUE_WHISPER_USE_WITH: Issue = Issue.create(
            "MissingUsage",
            "The Method should be used with the another method specified by @UseWith.",
            "You can not just invoke this method without using the method specified by the " +
                "annotation @UseWith. You should use them both within a class.",
            Category.CORRECTNESS,
            10,
            Severity.WARNING,
            Implementation(
                WhisperUseWithDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE)
            ))


        private const val useWithAnnotation = "$AnnotationPkg.UseWith"

        fun getIssue() = arrayOf(ISSUE_WHISPER_USE_WITH)
    }

    //override fun getApplicableUastTypes() = listOf(UClass::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return object : UElementHandler() {
            override fun visitClass(node: UClass) {
                System.out.println(node.asRecursiveLogString())
            }
        }
    }

    override fun isApplicableAnnotationUsage(type: AnnotationUsageType): Boolean {
        return type == AnnotationUsageType.METHOD_CALL
    }

    override fun applicableAnnotations() = listOf(useWithAnnotation)

    override fun visitAnnotationUsage(
        context: JavaContext,
        usage: UElement,
        type: AnnotationUsageType,
        annotation: UAnnotation,
        qualifiedName: String,
        method: PsiMethod?,
        annotations: List<UAnnotation>,
        allMemberAnnotations: List<UAnnotation>,
        allClassAnnotations: List<UAnnotation>,
        allPackageAnnotations: List<UAnnotation>
    ) {
        val useWithStr = annotation.attributeValues.firstOrNull()
            ?.expression?.evaluateString()
            ?: return

        val belongCls = method?.containingClass?.qualifiedName

        val call = usage as? UCallExpression ?: return

        val scope = usage.getContainingUClass()
            ?: usage.getContainingUFile() ?: return

        val availableCaller = call.getAvailableCaller()

        System.out.println("caller = ${availableCaller.joinToString { it.asSourceString() }}")

        var match = false
        scope.accept(object : DataFlowVisitor(availableCaller, availableCaller.mapNotNull { it.tryResolve() }) {

            override fun receiver(call: UCallExpression) {
                System.out.println("receiver ${call.methodName}")
                if (call.methodName == useWithStr) {
                    if (belongCls == call.resolve()?.containingClass?.qualifiedName) {
                        match = true
                    }
                }
            }

            override fun field(field: UElement) {
                System.out.println("field = $field")
            }

            override fun visitBinaryExpression(node: UBinaryExpression): Boolean {
                System.out.println("bin = $node")
                return super.visitBinaryExpression(node)
            }
            override fun afterVisitClass(node: UClass) {
                super.afterVisitClass(node)
                System.out.println("afterClass ${node.name}")
            }
        })

        if (!match) {
            report(context, usage, useWithStr)
        }
    }

    private fun report(context: JavaContext, usage: UElement, useWithStr: String) {
        val msg = "${usage.asSourceString()} must be used with $useWithStr"
        context.report(ISSUE_WHISPER_USE_WITH, usage, context.getLocation(usage), msg)
    }

    private fun UCallExpression.getAvailableCaller(): List<UElement> {
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
}