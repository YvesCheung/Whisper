package com.yy.mobile.whisperlint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.AnnotationUsageType
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintUtils.getMethodName
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiPrimitiveType
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.evaluateString
import org.jetbrains.uast.getContainingUClass
import org.jetbrains.uast.getContainingUFile
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

        val ISSUE_WHISPER_USE_WITH_WRONG_METHOD: Issue = Issue.create(
            "MissingMethod",
            "The method specified by @UseWith is absent.",
            "You should check if the method name is spelling wrong. This method should be " +
                "declared in the current class or in the return type of the @UseWith annotated method.",
            Category.CORRECTNESS,
            8,
            Severity.ERROR,
            Implementation(
                WhisperUseWithDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE)
            ))

        private const val useWithAnnotation = "$AnnotationPkg.UseWith"

        fun getIssue() = arrayOf(ISSUE_WHISPER_USE_WITH, ISSUE_WHISPER_USE_WITH_WRONG_METHOD)
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UMethod::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return object : UElementHandler() {

            private val evaluator = context.evaluator

            override fun visitMethod(node: UMethod) {
                val annotations = node.annotations
                val useWithAnnotation = annotations.find { it.qualifiedName == useWithAnnotation }
                    ?: return

                val useWithStr = useWithAnnotation.attributeValues.firstOrNull()
                    ?.expression?.evaluateString()
                    ?: return

                if (useWithStr == node.name) {
                    val msg = "@UseWith can not use the same parameters [$useWithStr] as the method name."
                    context.report(
                        ISSUE_WHISPER_USE_WITH_WRONG_METHOD,
                        node,
                        context.getLocation(node),
                        msg)
                    return
                }

                val methodItSelfClass = node.containingClass?.qualifiedName
                val methodReturnClass = node.returnType?.takeIf { it !is PsiPrimitiveType }?.canonicalText

                if (methodItSelfClass == null && methodReturnClass == null) {
                    val msg = "@UseWith can only annotate methods in classes or methods that " +
                        "have return type."
                    context.report(
                        ISSUE_WHISPER_USE_WITH_WRONG_METHOD,
                        node,
                        context.getLocation(node),
                        msg)
                    return
                }

                var found = false
                if (methodItSelfClass != null) {
                    val methods = evaluator.findClass(methodItSelfClass)
                        ?.findMethodsByName(useWithStr, true)
                    if (methods != null && methods.isNotEmpty()) {
                        found = true
                    }
                }

                if (!found && methodReturnClass != null) {
                    val methods = evaluator.findClass(methodReturnClass)
                        ?.findMethodsByName(useWithStr, true)
                    if (methods != null && methods.isNotEmpty()) {
                        found = true
                    }
                }

                if (!found) {
                    var msg = "Method [$useWithStr] should be declared"
                    if (methodItSelfClass != null) {
                        msg += if (methodReturnClass != null) {
                            " in [$methodItSelfClass] or"
                        } else {
                            " in [$methodItSelfClass]"
                        }
                    }
                    if (methodReturnClass != null) {
                        msg += " in [$methodReturnClass]."
                    }

                    context.report(
                        ISSUE_WHISPER_USE_WITH_WRONG_METHOD,
                        node,
                        context.getLocation(node),
                        msg)
                }
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
                val (elements, references, properties) = call.getAvailableReturnValue()
                match = findCleanUpMethod(scope, useWithStr, methodReturnClass,
                    elements, references, properties)
            }
        }

        if (!match) {
            val msg = "${usage.asSourceString()} should be used with $useWithStr"
            context.report(ISSUE_WHISPER_USE_WITH, usage, context.getLocation(usage), msg)
        }
    }

    private fun findCleanUpMethod(
        scope: UElement,
        methodShouldBeFound: String,
        ClassThatMethodShouldBelongTo: String,
        initElements: List<UElement> = emptyList(),
        initReferences: List<PsiElement> = emptyList(),
        initProperties: List<String> = emptyList()
    ): Boolean {
        var match = false
        scope.accept(object : DataFlowVisitor(initElements, initReferences, initProperties) {

            override fun visitElement(node: UElement) = match || super.visitElement(node)

            override fun receiver(call: UCallExpression) {
                if (getMethodName(call) == methodShouldBeFound) {
                    if (ClassThatMethodShouldBelongTo ==
                        call.resolve()?.containingClass?.qualifiedName) {
                        match = true
                    }
                }
            }
        })
        return match
    }
}