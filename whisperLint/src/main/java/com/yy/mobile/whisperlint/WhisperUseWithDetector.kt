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
import com.android.tools.lint.detector.api.getMethodName
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.asRecursiveLogString
import org.jetbrains.uast.evaluateString
import org.jetbrains.uast.getContainingUClass
import org.jetbrains.uast.getContainingUFile
import org.jetbrains.uast.getOutermostQualified
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.tryResolve
import org.jetbrains.uast.util.isConstructorCall
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

    override fun getApplicableUastTypes() = listOf(UClass::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return object : UElementHandler() {
            override fun visitClass(node: UClass) {
                System.out.println(node.asRecursiveLogString())
            }
        }
    }

    override fun isApplicableAnnotationUsage(type: AnnotationUsageType) =
        type == AnnotationUsageType.METHOD_CALL

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
        method ?: return
        val evaluator = context.evaluator

        val useWithStr = annotation.attributeValues.firstOrNull()
            ?.expression?.evaluateString()
            ?: return

        val methodItSelfClass = method.containingClass?.qualifiedName
        val methodReturnClass = method.returnType?.canonicalText


        val call = usage as? UCallExpression ?: return

        val scope = usage.getContainingUClass()
            ?: usage.getContainingUFile() ?: return

        var match = false

        if (methodItSelfClass != null) {
            val methods = evaluator.findClass(methodItSelfClass)?.findMethodsByName(useWithStr, true)
            if (methods != null && methods.isNotEmpty()) {
                // the method belongs to itself
                // so find the clean up method that belongs to itself
                val availableCaller = call.getAvailableCaller()
                val references = availableCaller
                    .filter {
                        !it.isConstructorCall()
                    }
                    .mapNotNull {
                        it.tryResolve()
                    }
                val elements = listOf(usage) + availableCaller.filter { it.isConstructorCall() }
                match = findCleanUpMethod(scope, useWithStr, methodItSelfClass, elements, references)
            }
        }

        if (!match && methodReturnClass != null) {
            val methods = evaluator.findClass(methodReturnClass)?.findMethodsByName(useWithStr, true)
            if (methods != null && methods.isNotEmpty()) {
                // the method belongs to return type
                // so find the clean up method that belongs to the return type
                val availableReturn = call.getAvailableReturnReference()
                val references = availableReturn.mapNotNull { it.tryResolve() }
                val elements = listOf(usage)
                match = findCleanUpMethod(scope, useWithStr, methodReturnClass, elements, references)
            }
        }

        if (!match) {
            report(context, usage, useWithStr)
        }
    }

    private fun findCleanUpMethod(
        scope: UElement,
        methodShouldBeFound: String,
        ClassThatMethodShouldBelongTo: String,
        initElements: List<UElement>,
        initReferences: List<PsiElement>
    ): Boolean {
        var match = false
        scope.accept(object : DataFlowVisitor(initElements, initReferences) {

            override fun visitElement(node: UElement) = match || super.visitElement(node)

            override fun receiver(call: UCallExpression) {
                System.out.println("receiver ${getMethodName(call)}")
                if (getMethodName(call) == methodShouldBeFound) {
                    if (ClassThatMethodShouldBelongTo ==
                        call.resolve()?.containingClass?.qualifiedName) {
                        match = true
                    }
                    System.out.println("match = ${call.resolve()} $match")
                }
            }
        })
        return match
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

    private fun UCallExpression.getAvailableReturnReference(): List<UElement> {
        val result = mutableListOf<UElement>()
        val qualified = this.getOutermostQualified()
        if (qualified != null) {
            System.out.println("qualified = $qualified")

            val assign = qualified.uastParent
            if (assign is UBinaryExpression) {
                result.add(assign.leftOperand)
            } else if (assign is UVariable) {
                result.add(assign)
            }
        }

        return result
    }
}