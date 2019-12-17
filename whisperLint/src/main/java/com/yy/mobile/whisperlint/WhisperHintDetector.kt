package com.yy.mobile.whisperlint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.AnnotationUsageType
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.ConstantEvaluator
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UElement
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.evaluateString
import java.util.*

/**
 * Created by 张宇 on 2018/8/13.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
class WhisperHintDetector : Detector(), Detector.UastScanner {

    companion object {

        val ISSUE_WHISPER_WARNING: Issue = Issue.create(
            "WhisperWarning",
            "Warning hints provided by the whisper lib.",
            "The corresponding message is provided by @NeedWarning annotation method or field",
            Category.USABILITY,
            1,
            Severity.WARNING,
            Implementation(
                WhisperHintDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE)
            ))

        val ISSUE_WHISPER_ERROR: Issue = Issue.create(
            "WhisperError",
            "Error message provided by the whisper lib.",
            "The corresponding message is provided by @NeedError annotation method or field",
            Category.USABILITY,
            1,
            Severity.ERROR,
            Implementation(
                WhisperHintDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE)
            ))

        val ISSUE_WHISPER_INFO: Issue = Issue.create(
            "WhisperInfo",
            "Message provided by the whisper lib.",
            "The corresponding message is provided by @NeedInfo annotation method or field",
            Category.USABILITY,
            1,
            Severity.INFORMATIONAL,
            Implementation(
                WhisperHintDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE)
            ))

        private const val needWarningAnnotation = "$AnnotationPkg.NeedWarning"
        private const val needErrorAnnotation = "$AnnotationPkg.NeedError"
        private const val needInfoAnnotation = "$AnnotationPkg.NeedInfo"

        fun getIssue() = arrayOf(
            ISSUE_WHISPER_WARNING,
            ISSUE_WHISPER_ERROR,
            ISSUE_WHISPER_INFO)
    }

    override fun isApplicableAnnotationUsage(type: AnnotationUsageType): Boolean {
        return type == AnnotationUsageType.METHOD_CALL ||
            type == AnnotationUsageType.METHOD_CALL_PARAMETER ||
            type == AnnotationUsageType.ANNOTATION_REFERENCE
    }

    override fun applicableAnnotations() = listOf(
        needWarningAnnotation, needErrorAnnotation, needInfoAnnotation)

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

        val reportMsg = annotation.attributeValues.firstOrNull()
            ?.expression?.evaluateString() ?: return

        val issue = when (annotation.qualifiedName) {
            needWarningAnnotation -> ISSUE_WHISPER_WARNING
            needErrorAnnotation -> ISSUE_WHISPER_ERROR
            needInfoAnnotation -> ISSUE_WHISPER_INFO
            else -> return
        }

        context.report(issue, usage, context.getLocation(usage), reportMsg)
    }

    override fun getApplicableUastTypes() = listOf(USimpleNameReferenceExpression::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler = WhisperHintHandler(context)

    private class WhisperHintHandler(private val context: JavaContext) : UElementHandler() {

        override fun visitSimpleNameReferenceExpression(node: USimpleNameReferenceExpression) {
            val psiField = (node.resolve() as? PsiField) ?: return
            val modifierList = psiField.modifierList ?: return

            val anno: PsiAnnotation = modifierList.findAnnotation(needErrorAnnotation)
                ?: modifierList.findAnnotation(needWarningAnnotation)
                ?: modifierList.findAnnotation(needInfoAnnotation)
                ?: return

            val psiAnnoValue = anno.parameterList.attributes.firstOrNull()?.value
            val hint = (ConstantEvaluator().evaluate(psiAnnoValue) as? String)
                ?: return

            val issue = when (anno.qualifiedName) {
                needWarningAnnotation -> ISSUE_WHISPER_WARNING
                needErrorAnnotation -> ISSUE_WHISPER_ERROR
                needInfoAnnotation -> ISSUE_WHISPER_INFO
                else -> return
            }

            context.report(issue, node, context.getLocation(node), hint)
        }
    }
}