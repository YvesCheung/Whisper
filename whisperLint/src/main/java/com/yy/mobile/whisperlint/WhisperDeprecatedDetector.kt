package com.yy.mobile.whisperlint

import com.android.tools.lint.detector.api.AnnotationUsageType
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiMethod
import com.yy.mobile.whisperlint.support.api2.AnnotationUsageTypeCompat
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UastCallKind
import org.jetbrains.uast.evaluateString
import org.jetbrains.uast.kotlin.KotlinUFunctionCallExpression
import java.util.*

/**
 * @author YvesCheung
 * 2018/8/13.
 *
 */
class WhisperDeprecatedDetector : Detector(), Detector.UastScanner {

    companion object {

        val ISSUE_WHISPER_DEPRECATED_WARNING: Issue = Issue.create(
            "DeprecatedWarning",
            "deprecated methods",
            "The methods are outdated. Please replace them with the new ones.",
            Category.USABILITY,
            5,
            Severity.WARNING,
            Implementation(
                WhisperDeprecatedDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE)
            ))

        val ISSUE_WHISPER_DEPRECATED_ERROR: Issue = Issue.create(
            "DeprecatedError",
            "deprecated methods",
            "The methods are outdated. Please replace them with the new ones.",
            Category.USABILITY,
            5,
            Severity.ERROR,
            Implementation(
                WhisperDeprecatedDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE)
            ))

        private const val deprecatedAnnotation = "$AnnotationPkg.DeprecatedBy"

        fun getIssue() = arrayOf(
            ISSUE_WHISPER_DEPRECATED_WARNING,
            ISSUE_WHISPER_DEPRECATED_ERROR)
    }

    override fun isApplicableAnnotationUsage(type: AnnotationUsageType): Boolean {
        return type in AnnotationUsageTypeCompat.setOf(AnnotationUsageTypeCompat.METHOD_CALL)
    }

    override fun applicableAnnotations() = listOf(deprecatedAnnotation)

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

        val methodCall = usage as? UCallExpression ?: return

        val arguments = methodCall.valueArguments.map { it.asSourceString() }.toTypedArray()
        val receiver = annotation.findAttributeValue("receiver")?.evaluateString()
        val replace = annotation.findAttributeValue("replaceWith")?.evaluateString() ?: return
        val message = annotation.findAttributeValue("message")?.evaluateString()
        val level = annotation.findAttributeValue("level")?.asSourceString()

        val issue: Issue =
            if (level == null || level.contains("Warning"))
                ISSUE_WHISPER_DEPRECATED_WARNING
            else
                ISSUE_WHISPER_DEPRECATED_ERROR

        val methodReceiver =
            if (receiver == null || receiver.isBlank()) {
                val receiverSrc = methodCall.receiver
                if (receiverSrc != null) {
                    if (receiverSrc is KotlinUFunctionCallExpression &&
                        receiverSrc.kind == UastCallKind.CONSTRUCTOR_CALL) {
                        receiverSrc.asSourceString().replace("<init>",
                            receiverSrc.classReference.resolvedName ?: "")
                    } else {
                        receiverSrc.asSourceString()
                    }
                } else {
                    ""
                }
            } else {
                receiver
            }

        val methodSelector =
            try {
                replace.format(*arguments)
            } catch (e: IllegalFormatException) {
                replace
            }

        val code =
            if (methodReceiver.isBlank())
                methodSelector
            else
                "$methodReceiver.$methodSelector"

        val hint: String =
            if (message == null || message.isEmpty())
                "Use $code instead of this method."
            else
                message

        val fix = LintFix.create()
            .replace()
            .name("replace with $code")
            .with(code)
            .reformat(true)
            .shortenNames()
            .build()
        context.report(issue, usage, context.getLocation(usage), hint, fix)
    }
}