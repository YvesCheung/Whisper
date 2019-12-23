package com.yy.mobile.whisperlint.ast

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import org.jetbrains.annotations.TestOnly
import org.jetbrains.uast.UElement
import java.util.*

/**
 * @author YvesCheung
 * 2019-12-19
 */
@Suppress("unused")
class ASTLogger : Detector(), Detector.UastScanner {

    companion object {

        private val Logger: Issue = Issue.create(
            "JustForLog",
            "logger",
            "visit the AST",
            Category.LINT,
            1,
            Severity.INFORMATIONAL,
            Implementation(ASTLogger::class.java, EnumSet.of(Scope.JAVA_FILE)))

        @TestOnly
        fun getIssues() = arrayOf(Logger)
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>> =
        listOf(UElement::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler =
        LogHandler(context)

    private inner class LogHandler(context: JavaContext) : UElementHandler() {

        override fun visitElement(node: UElement) {
            log { "visitElement ${node.asLogString()} $node" }
        }
    }

    private fun log(msg: () -> Any?) = println(msg().toString())
}
