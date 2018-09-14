package com.yy.mobile.whisperlint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.AnnotationUsageType
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.getMethodName
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.PsiVariable
import org.jetbrains.uast.UAnnotated
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UField
import org.jetbrains.uast.UParameter
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.UReturnExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.asRecursiveLogString
import org.jetbrains.uast.getContainingClass
import org.jetbrains.uast.getContainingUClass
import org.jetbrains.uast.getContainingUFile
import org.jetbrains.uast.getContainingUMethod
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
            "Iterator, Entry, Collection, Map that annotated by @Immutable cannot be " +
                "modified.",
            Category.CORRECTNESS,
            7,
            Severity.ERROR,
            Implementation(
                WhisperImmutableDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE)
            ))

        val ISSUE_WHISPER_MISSING_IMMUTABLE: Issue = Issue.create(
            "ImmutableEscape",
            "The reference annotated by @Immutable has escaped.",
            "Can not assign an immutable object to a mutable object.",
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
            "put",
            "putFirst",
            "putLast",
            "poll",
            "pollFirst",
            "pollLast",
            "removeFirstOccurrence",
            "removeLastOccurrence",
            "push",
            "pop",
            "drainTo"
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

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(
        UClass::class.java,
        UField::class.java,
        UCallExpression::class.java,
        USimpleNameReferenceExpression::class.java,
        UParameter::class.java)

    private val checkFieldsToScope = mutableSetOf<Pair<PsiElement, UElement>>()

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return object : UElementHandler() {

            override fun visitClass(node: UClass) {
                System.out.println(node.asRecursiveLogString())
            }

            override fun visitField(node: UField) {

                val initializer = node.uastInitializer
                //if uastInitializer is not null,
                //`visitAnnotationUsage` for type ASSIGNMENT will be invoked.
                //note: kotlin `by lazy` is a special case
                if (initializer == null && checkIsImmutable(node.annotations)) {
                    val scope = node.getContainingUClass() ?: return
                    val instances = listOf(node)
                    val references = listOfNotNull(node.sourcePsi, node.javaPsi)
                    val properties = listOfNotNull(node.text)
                    deepSearchUsage(context, context.getLocation(node), scope,
                        instances = instances,
                        references = references,
                        properties = properties)
                }
                //if the field does not annotated by @Immutable
                //check whether the initializer is mutable.
                else if (initializer != null && !checkIsImmutable(node.annotations)) {

                    if (checkIsImmutable(initializer)) {
                        reportField(context, initializer, node)
                    }
                }
            }

            override fun visitCallExpression(node: UCallExpression) {
                val scope = node.getContainingUMethod() ?: return
                node.getAvailableCaller()
                    .mapNotNull { receiver ->
                        val psiVar = receiver.tryResolve() as? PsiField
                        val currentName = receiver.getContainingUClass()?.qualifiedName
                        val psiName = psiVar.getContainingClass()?.qualifiedName
                        if (psiVar != null && psiName != currentName && checkIsImmutable(psiVar)) {
                            psiVar
                        } else {
                            null
                        }
                    }.forEach { field ->
                        checkFieldsToScope.add(field to scope)
                    }
            }

            override fun visitSimpleNameReferenceExpression(node: USimpleNameReferenceExpression) {
                val scope = node.getContainingUMethod() ?: return
                val psiRef = node.resolve() as? PsiField ?: return
                val currentName = node.getContainingUClass()?.qualifiedName
                val psiName = psiRef.containingClass?.qualifiedName
                if (psiName != currentName && checkIsImmutable(psiRef)) {
                    checkFieldsToScope.add(psiRef to scope)
                }
            }

            override fun visitParameter(node: UParameter) {
                if (checkIsImmutable(node.annotations)) {
                    val scope = node.getContainingUMethod() ?: return
                    node.javaPsi?.let { checkFieldsToScope.add(it to scope) }
                    node.sourcePsi?.let { checkFieldsToScope.add(it to scope) }
                }
            }
        }
    }

    override fun beforeCheckFile(context: Context) {
        checkFieldsToScope.clear()
    }

    override fun afterCheckFile(context: Context) {
        if (context is JavaContext) {
            for ((field, scope) in checkFieldsToScope) {
                deepSearchUsage(context, context.getLocation(field), scope,
                    references = listOf(field))
            }
        }
    }

    override fun applicableAnnotations() = listOf(immutableAnnotation)

    override fun isApplicableAnnotationUsage(type: AnnotationUsageType): Boolean {
        return type == AnnotationUsageType.ASSIGNMENT ||
            type == AnnotationUsageType.METHOD_CALL
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
        usage as? UExpression ?: return

        val scope: UElement = usage.getContainingUMethod() as? UElement
            ?: usage.getContainingUClass()
            ?: usage.getContainingUFile()
            ?: return

        val (instances, references, properties) = usage.getAvailableReturnValue()

        deepSearchUsage(context, context.getLocation(usage), scope, instances, references, properties)
    }

    private fun deepSearchUsage(
        context: JavaContext,
        usage: Location,
        scope: UElement,
        instances: List<UElement> = emptyList(),
        references: List<PsiElement> = emptyList(),
        properties: List<String> = emptyList()
    ) {
        scope.accept(object : DataFlowVisitor(instances, references, properties) {

            private var abort = false

            override fun visitElement(node: UElement) = abort || super.visitElement(node)

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
                    isVectorOrStackMethod(call, context)) {

                    val mainMsg = "you cannot invoke the [$call] method on an immutable object."
                    val referenceMsg = "This reference is annotated by @Immutable"
                    context.report(
                        ISSUE_WHISPER_IMMUTABLE,
                        call,
                        context.getLocation(call).withSecondary(usage, referenceMsg),
                        mainMsg)
                }
            }

            override fun field(assignment: UElement, field: PsiField) {
                System.out.println("field = $field assign = ${assignment.asSourceString()}")
                if (!checkIsImmutable(field)) {
                    abort = true
                    reportField(context, assignment, field)
                }
            }

            override fun returns(expression: UReturnExpression) {
                System.out.println("return = $expression")
                val method = expression.getContainingUMethod() ?: return
                if (!checkIsImmutable(method as PsiModifierListOwner)) {
                    abort = true
                    val mainMsg = "Unable to return an immutable expression within a " +
                        "method without @Immutable annotation."
                    val referenceMsg = "This expression [${expression.asSourceString()}] " +
                        "is immutable."
                    val methodLocation = context.getNameLocation(method)
                    val refLocation = context.getLocation(expression)
                    val quickFix = LintFix.create()
                        .name("Annotate method [${method.name}] with @Immutable")
                        .replace()
                        .range(context.getLocation(method))
                        .with("@$immutableAnnotation ${method.asSourceString()}")
                        .shortenNames()
                        .reformat(true)
                        .build()
                    context.report(ISSUE_WHISPER_MISSING_IMMUTABLE,
                        method,
                        methodLocation.withSecondary(refLocation, referenceMsg),
                        mainMsg,
                        quickFix)
                }
            }
        })
    }

    private fun reportField(
        context: JavaContext,
        assignment: UElement,
        field: PsiField
    ) {
        val assignMsg = assignment.asSourceString()
        val mainMsg = "Unable to assign an immutable object [$assignMsg] " +
            "to a mutable field [${field.name}]."
        val referenceMsg = "This expression [$assignMsg] is immutable."
        val referenceLocation = context.getLocation(assignment)
        val fieldLocation = context.getLocation(field)
        val quickFix = LintFix.create()
            .name("Annotate [${field.name}] with @Immutable")
            .replace()
            .range(fieldLocation)
            .with("@$immutableAnnotation ${field.text}")
            .shortenNames()
            .reformat(true)
            .build()
        context.report(
            ISSUE_WHISPER_MISSING_IMMUTABLE,
            field,
            fieldLocation.withSecondary(referenceLocation, referenceMsg),
            mainMsg,
            quickFix)
    }

    private fun checkIsImmutable(element: UElement): Boolean {
        if (element is UCallExpression) {
            val method = element.resolve()
            if (method != null && checkIsImmutable(method)) {
                return true
            }
        } else if (element is UQualifiedReferenceExpression) {
            return checkIsImmutable(element.selector)
        } else if (element is USimpleNameReferenceExpression) {
            val variable = element.tryResolve() as? PsiVariable
            if (variable != null && checkIsImmutable(variable)) {
                return true
            }
        }
        if (element is PsiModifierListOwner) {
            return checkIsImmutable(element as PsiModifierListOwner)
        }
        if (element is UAnnotated) {
            return checkIsImmutable(element.annotations)
        }
        return false
    }

    private fun checkIsImmutable(element: List<org.jetbrains.uast.UAnnotation>): Boolean {
        return element.find { it.qualifiedName == immutableAnnotation } != null
    }

    private fun checkIsImmutable(element: PsiModifierListOwner): Boolean {
        return element.annotations.find { it.qualifiedName == immutableAnnotation } != null
    }

    private fun isEntryMethod(call: UCallExpression, context: JavaContext) =
        call.isMethodOf(context, entryMethods, entryCls)

    private fun isMapMethod(call: UCallExpression, context: JavaContext) =
        call.isMethodOf(context, mapMathods, mapCls)

    private fun isVectorOrStackMethod(call: UCallExpression, context: JavaContext) =
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