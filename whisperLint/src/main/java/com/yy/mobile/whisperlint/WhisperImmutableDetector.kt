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
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UField
import org.jetbrains.uast.asRecursiveLogString
import org.jetbrains.uast.getContainingClass
import org.jetbrains.uast.getContainingUClass
import org.jetbrains.uast.getContainingUFile
import org.jetbrains.uast.tryResolve
import java.util.*

/**
 * Created by 张宇 on 2018/9/8.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
class WhisperImmutableDetector : Detector(), Detector.UastScanner {

    companion object {

        val ISSUE_WHISPER_IMMUTABLE: Issue = Issue.create(
            "ImmutableObject",
            "The reference annotated by @Immutable should not be modified.",
            "aaa",
            Category.CORRECTNESS,
            10,
            Severity.WARNING,
            Implementation(
                WhisperImmutableDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE)
            ))

        private const val immutableAnnotation = "$AnnotationPkg.Immutable"

        fun getIssue() = arrayOf(ISSUE_WHISPER_IMMUTABLE)

        private val collectionMethod = setOf(
            "add",
            "addAll",
            "remove",
            "removeAll",
            "removeIf",
            "retainAll",
            "clear"
        )
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UClass::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return object : UElementHandler() {

            override fun visitClass(node: UClass) {
                System.out.println(node.asRecursiveLogString())
            }
        }
    }

    override fun applicableAnnotations() = listOf(immutableAnnotation)

    override fun isApplicableAnnotationUsage(type: AnnotationUsageType): Boolean {
//        return type == AnnotationUsageType.VARIABLE_REFERENCE ||
//            type == AnnotationUsageType.METHOD_CALL
        return super.isApplicableAnnotationUsage(type)
    }

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
        val exp = usage as? UExpression ?: return

        val scope = usage.getContainingUClass()
            ?: usage.getContainingUFile() ?: return

        if (type == AnnotationUsageType.ASSIGNMENT) {
            val availableReturn = exp.getAvailableReturnReference()
            val references = availableReturn.mapNotNull { it.tryResolve() }
            val properties = availableReturn.mapNotNull { (it as? UField)?.text }
            val instances = listOf(usage)

            System.out.println(
                "reference = ${references.joinToString { it.text }}\n" +
                    "properties = $properties\n" +
                    "instances = ${instances.joinToString { it.asSourceString() }}"
            )

            var match = false

            scope.accept(object : DataFlowVisitor(instances, references, properties) {

                override fun visitElement(node: UElement) = match || super.visitElement(node)

                override fun receiver(call: UCallExpression) {
                    System.out.println("receiver = $call class = ${call.getContainingClass()}")
                    if (collectionMethod.contains(getMethodName(call))) {
                        context.report(
                            ISSUE_WHISPER_IMMUTABLE,
                            call,
                            context.getLocation(call),
                            "immutable object")
                    }
                }
            })
        }
    }
}