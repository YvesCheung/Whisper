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
import org.jetbrains.uast.asRecursiveLogString
import org.jetbrains.uast.getContainingUClass
import org.jetbrains.uast.getContainingUFile
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
            "Iterator, Entry, Collection, Map that annotated by @Immutable cannot be " +
                "modified.",
            Category.CORRECTNESS,
            7,
            Severity.ERROR,
            Implementation(
                WhisperImmutableDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE)
            ))

        private const val immutableAnnotation = "$AnnotationPkg.Immutable"

        fun getIssue() = arrayOf(ISSUE_WHISPER_IMMUTABLE)

        private val checkReturnMethods = setOf(
            "iterator",
            "listIterator",
            "getIterator",
            "descendingIterator",
            "subSet",
            "subList",
            "subMap",
            "keySet",
            "entrySet",
            "values",
            "headMap",
            "tailMap",
            "headSet",
            "tailSet")

        private const val collectionCls = "java.util.Collection"

        private val collectionMethods = setOf(
            "add",
            "addAll",
            "remove",
            "removeAll",
            "removeIf",
            "retainAll",
            "clear"
        )

        private const val listCls = "java.util.List"

        private val listMethods = setOf(
            "set",
            "removeRange",
            "replaceAll",
            "sort"
        )

        private const val queCls = "java.util.Queue"

        private val queMethods = setOf(
            "addFirst",
            "addLast",
            "offer",
            "offerFirst",
            "offerLast",
            "removeFirst",
            "removeLast",
            "poll",
            "pollFirst",
            "pollLast",
            "removeFirstOccurrence",
            "removeLastOccurrence",
            "push",
            "pop"
        )

        private const val vectorCls = "java.util.Vector"

        private val vectorAndStackMethods = setOf(
            "push",
            "pop",
            "addElement",
            "removeElement",
            "removeAllElements",
            "insertElementAt",
            "removeElementAt"
        )

        private const val mapCls = "java.util.Map"

        private val mapMathods = setOf(
            "put",
            "putAll",
            "putIfAbsent",
            "remove",
            "merge",
            "replace",
            "replaceAll",
            "pollFirstEntry",
            "pollLastEntry",
            "clear"
        )

        private const val iteratorCls = "java.util.Iterator"

        private val iteratorMethods = setOf(
            "remove",
            "set",
            "add"
        )

        private const val entryCls = "java.util.Map.Entry"

        private val entryMethods = setOf(
            "setValue"
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

        if (type == AnnotationUsageType.ASSIGNMENT ||
            type == AnnotationUsageType.METHOD_CALL) {

            val (instances, references, properties) = exp.getAvailableReturnValue()

            scope.accept(object : DataFlowVisitor(instances, references, properties) {

                override fun receiver(call: UCallExpression) {
                    System.out.println("receiver = $call class = ${call.resolve()?.containingClass}")

                    if (checkReturnMethods.contains(getMethodName(call))) {
                        val (iteIns, iteRef, iteProp) = call.getAvailableReturnValue()
                        this.instances.addAll(iteIns)
                        this.references.addAll(iteRef)
                        this.properties.addAll(iteProp)

                    } else if (isCollectionMethod(call, context) ||
                        isIteratorMethod(call, context) ||
                        isEntryMethod(call, context) ||
                        isListMethod(call, context) ||
                        isMapMethod(call, context) ||
                        isQueueMethod(call, context) ||
                        isVetorOrStackMethod(call, context)) {

                        val mainMsg = "you cannot invoke the [$call] method on an immutable object."
                        val referenceMsg = "This reference is annotated by @Immutable"
                        val referenceLocation = context.getLocation(usage)
                        context.report(
                            ISSUE_WHISPER_IMMUTABLE,
                            call,
                            context.getLocation(call).withSecondary(referenceLocation, referenceMsg),
                            mainMsg)
                    }
                }
            })
        }
    }

    private fun isEntryMethod(call: UCallExpression, context: JavaContext) =
        call.isMethodOf(context, entryMethods, entryCls)

    private fun isMapMethod(call: UCallExpression, context: JavaContext) =
        call.isMethodOf(context, mapMathods, mapCls)

    private fun isVetorOrStackMethod(call: UCallExpression, context: JavaContext) =
        call.isMethodOf(context, vectorAndStackMethods, vectorCls)

    private fun isQueueMethod(call: UCallExpression, context: JavaContext) =
        call.isMethodOf(context, queMethods, queCls)

    private fun isListMethod(call: UCallExpression, context: JavaContext) =
        call.isMethodOf(context, listMethods, listCls)

    private fun isCollectionMethod(call: UCallExpression, context: JavaContext) =
        call.isMethodOf(context, collectionMethods, collectionCls)

    private fun isIteratorMethod(call: UCallExpression, context: JavaContext) =
        call.isMethodOf(context, iteratorMethods, iteratorCls)

    private fun UCallExpression.isMethodOf(
        context: JavaContext,
        names: Collection<String>,
        clsName: String
    ): Boolean {
        if (names.contains(getMethodName(this))) {
            val method = this.resolve()
            return method != null &&
                context.evaluator.isMemberInSubClassOf(method, clsName, false)
        }
        return false
    }
}