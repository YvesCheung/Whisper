package com.yy.mobile.whisperlint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.UastLintUtils.Companion.getAnnotationStringValue
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiField
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.getUastParentOfType
import java.util.*

/**
 * @author YvesCheung
 * 2018/8/13.
 *
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

    override fun getApplicableUastTypes() = listOf<Class<out UElement>>(
        USimpleNameReferenceExpression::class.java,
        UCallExpression::class.java
    )

    override fun createUastHandler(context: JavaContext): UElementHandler = WhisperHintHandler(context)

    private class WhisperHintHandler(private val context: JavaContext) : UElementHandler() {

        override fun visitSimpleNameReferenceExpression(node: USimpleNameReferenceExpression) {
            val psiField = (node.resolve() as? PsiField) ?: return
            val modifierList = psiField.modifierList ?: return

            val anno: PsiAnnotation = modifierList.findAnnotation(needErrorAnnotation)
                ?: modifierList.findAnnotation(needWarningAnnotation)
                ?: modifierList.findAnnotation(needInfoAnnotation)
                ?: return

            reportAnnotation(anno, node)
        }

        override fun visitCallExpression(node: UCallExpression) {
            val method = node.resolve() ?: return
            val annotation = method.getAnnotation(needErrorAnnotation)
                ?: method.getAnnotation(needWarningAnnotation)
                ?: method.getAnnotation(needInfoAnnotation)
                ?: return

            reportAnnotation(annotation, node)
        }

        private fun reportAnnotation(anno: PsiAnnotation, node: UElement) {
            val uast = anno.getUastParentOfType(UAnnotation::class.java)
                ?: return
            if (uast.qualifiedName == anno.qualifiedName) {
                val hint = getAnnotationStringValue(uast, "value") ?: return

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
}