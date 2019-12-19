package com.yy.mobile.whisperlint

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
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.evaluateString
import org.jetbrains.uast.getContainingUClass
import java.util.*

/**
 * @author YvesCheung
 * 2018/9/3.
 *
 */
class WhisperHideDetector : Detector(), Detector.UastScanner {

    companion object {

        val ISSUE_WHISPER_HIDE: Issue = Issue.create(
            "HideMember",
            "Methods/Fields that can only be accessed in specified classes.",
            "This method/field is partially visible. And can only be accessed in the class, " +
                "which is declared as 'friend' with annotation @Hide.",
            Category.A11Y,
            9,
            Severity.ERROR,
            Implementation(
                WhisperHideDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE)
            ))

        private const val hideAnnotation = "$AnnotationPkg.Hide"

        fun getIssue() = arrayOf(ISSUE_WHISPER_HIDE)
    }

    override fun isApplicableAnnotationUsage(type: AnnotationUsageType): Boolean {
        return type == AnnotationUsageType.METHOD_CALL
    }

    override fun applicableAnnotations() = listOf(hideAnnotation)

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
        val methodInClass = method?.containingClass ?: return

        val friendClsStr = annotation.attributeValues.firstOrNull()?.expression
            as? UCallExpression ?: return

        val friendClsSet = friendClsStr.valueArguments
            .map {
                it.evaluateString()
            }
            .toMutableSet()
            .apply {
                add(methodInClass.qualifiedName)
            }

        var shouldReport = true

        var outerCls: UClass? = usage.getContainingUClass()
        while (outerCls != null) {
            val quaName = outerCls.qualifiedName
            val simpleName = outerCls.name

            if (friendClsSet.contains(quaName) ||
                friendClsSet.contains(simpleName)) {
                shouldReport = false
                break
            }

            if (outerCls.isStatic) {
                break
            }
            outerCls = outerCls.getContainingUClass()
        }

        if (shouldReport) {
            val msg = "Methods that can only be accessed in $friendClsSet"
            context.report(
                ISSUE_WHISPER_HIDE,
                usage,
                context.getNameLocation(usage),
                msg)
        }
    }
}