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
import com.android.tools.lint.detector.api.LintUtils.getMethodName
import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiVariable
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.yy.mobile.whisperlint.ast.DataFlowVisitor
import com.yy.mobile.whisperlint.ast.getAvailableCaller
import com.yy.mobile.whisperlint.ast.getAvailableReturnValue
import com.yy.mobile.whisperlint.support.api2.AnnotationUsageTypeCompat
import org.jetbrains.uast.UAnnotated
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UField
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UParameter
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.UReturnExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.getContainingClass
import org.jetbrains.uast.getContainingUClass
import org.jetbrains.uast.getContainingUFile
import org.jetbrains.uast.getContainingUMethod
import org.jetbrains.uast.tryResolve
import java.util.*

/**
 * @author YvesCheung
 * 2018/9/8.
 *
 */
class WhisperImmutableDetector : Detector(), Detector.UastScanner {

    companion object {

        private const val immutableAnnotation = "$AnnotationPkg.Immutable"

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
            "tailSet",
            "next")

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

        private val aimCls = listOf(
            collectionCls,
            mapCls,
            entryCls,
            iteratorCls
        )

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

        val ISSUE_WHISPER_OVERRIDE_IMMUTABLE: Issue = Issue.create(
            "ImmutableOverride",
            "The method or parameter should annotate with @Immutable.",
            "The method in the super class inherited by this method has " +
                "the return value or parameter annotated by @Immutable. Please annotate " +
                "this method with @Immutable too.",
            Category.CORRECTNESS,
            6,
            Severity.ERROR,
            Implementation(
                WhisperImmutableDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE)
            ))

        val ISSUE_WHISPER_IMMUTABLE_TARGET: Issue = Issue.create(
            "ImmutableTarget",
            "Types that cannot be annotated by @Immutable.",
            "The class or super class of target annotated by @Immutable must " +
                "be one of the following:\n" +
                aimCls.joinToString { "- $it\n" },
            Category.CORRECTNESS,
            8,
            Severity.WARNING,
            Implementation(
                WhisperImmutableDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE)
            )
        )

        fun getIssue() = arrayOf(
            ISSUE_WHISPER_IMMUTABLE,
            ISSUE_WHISPER_MISSING_IMMUTABLE,
            ISSUE_WHISPER_OVERRIDE_IMMUTABLE,
            ISSUE_WHISPER_IMMUTABLE_TARGET)
    }

    override fun getApplicableUastTypes() = listOf(
        UField::class.java,
        UCallExpression::class.java,
        USimpleNameReferenceExpression::class.java,
        UParameter::class.java,
        UVariable::class.java,
        UMethod::class.java)

    private val checkFieldsToScope = mutableSetOf<Pair<PsiElement, UElement>>()

    private val reportFieldsToAssignment = mutableSetOf<Pair<PsiField, UElement>>()

    private val reportArgumentToFunction = mutableSetOf<Pair<UElement, UCallExpression>>()

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return object : UElementHandler() {

            override fun visitField(node: UField) {

                val initializer = node.uastInitializer
                //if uastInitializer is not null,
                //`visitAnnotationUsage` for type ASSIGNMENT will be invoked.
                //note: kotlin `by lazy` is a special case
                if (initializer == null && checkIsImmutable(node.annotations)) {
                    val scope = node.getContainingUClass() ?: return
                    val instances = listOf(node)
                    //val references = listOfNotNull(node.sourcePsi, node.javaPsi)
                    val references = listOfNotNull(node.psi)
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
                        reportFieldsToAssignment.add(node to initializer)
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
                    // node.javaPsi?.let { checkFieldsToScope.add(it to scope) }
                    // node.sourcePsi?.let { checkFieldsToScope.add(it to scope) }
                    node.psi.let { checkFieldsToScope.add(it to scope) }
                }
            }

            override fun visitVariable(node: UVariable) {
                if (checkIsImmutable(node.annotations)) {
                    val cls = (node.type as? PsiClassType)?.resolve()
                    checkType(cls, node, node.type.canonicalText)
                }
            }

            override fun visitMethod(node: UMethod) {
                val methods = node.findSuperMethods()
                val currentIsImmutable = checkIsImmutable(node.annotations)
                if (!currentIsImmutable) {
                    val superIsImmutable = methods.any { checkIsImmutable(it) }
                    if (superIsImmutable) {
                        val msg = "The method [${node.getSign()}] without @Immutable cannot override " +
                            "@Immutable method."
                        val quickFix = LintFix.create()
                            .replace()
                            .name("add @Immutable annotation")
                            .range(context.getLocation(node))
                            .with("@$immutableAnnotation ${node.asSourceString()}")
                            .shortenNames()
                            .reformat(true)
                            .build()
                        context.report(
                            ISSUE_WHISPER_OVERRIDE_IMMUTABLE,
                            node,
                            context.getNameLocation(node),
                            msg,
                            quickFix
                        )
                    }
                } else { //is Immutable
                    val cls = (node.returnType as? PsiClassType)?.resolve()
                    checkType(cls, node, node.returnType?.canonicalText)
                }

                val lintFlag = BooleanArray(node.parameters.size)
                val currentFlag = obtainIdxForImmutable(node)
                if (currentFlag.all { it }) {
                    return
                }
                for (method in methods) {
                    method.parameters.forEachIndexed { index, parameter ->
                        if (!currentFlag[index]) {
                            if (parameter is PsiParameter &&
                                checkIsImmutable(parameter)) {
                                currentFlag[index] = true
                                lintFlag[index] = true
                            }
                        }
                    }
                }

                lintFlag.forEachIndexed { index, b ->
                    if (b) {
                        val param = node.parameters[index] as? PsiParameter ?: return
                        val msg = "The parameter [${param.text}] without @Immutable cannot override " +
                            "@Immutable parameter."
                        val quickFix = LintFix.create()
                            .replace()
                            .name("add @Immutable annotation")
                            .range(context.getLocation(param))
                            .with("@$immutableAnnotation ${param.text}")
                            .build()
                        context.report(
                            ISSUE_WHISPER_OVERRIDE_IMMUTABLE,
                            param,
                            context.getNameLocation(param),
                            msg,
                            quickFix
                        )
                    }
                }
            }

            private fun obtainIdxForImmutable(method: PsiMethod): BooleanArray {
                val flagArray = BooleanArray(method.parameters.size)
                method.parameters.forEachIndexed { index, parameter ->
                    flagArray[index] = parameter is PsiParameter && checkIsImmutable(parameter)
                }
                return flagArray
            }

            private fun checkType(cls: PsiClass?, node: UElement, currentMsg: String?) {
                val evaluator = context.evaluator

                fun clsExtendIt(superCls: String): Boolean =
                    evaluator.extendsClass(cls, superCls, false)

                if (cls == null || !aimCls.any(::clsExtendIt)) {
                    val msg = "Only class $aimCls or their subclass can be annotated by @Immutable. " +
                        "\nbut current is [$currentMsg]"
                    context.report(
                        ISSUE_WHISPER_IMMUTABLE_TARGET,
                        node,
                        context.getNameLocation(node),
                        msg)
                }
            }
        }
    }

    override fun beforeCheckFile(context: Context) {
        checkFieldsToScope.clear()
        reportFieldsToAssignment.clear()
        reportArgumentToFunction.clear()
    }

    override fun afterCheckFile(context: Context) {
        if (context is JavaContext) {
            for ((field, scope) in checkFieldsToScope) {
                deepSearchUsage(context, context.getLocation(field), scope,
                    references = listOf(field))
            }

            reportFieldsToAssignment.forEach { (field, assignment) ->
                reportField(context, assignment, field)
            }
            reportArgumentToFunction.forEach { (argument, function) ->
                reportArgument(context, function, argument)
            }
        }
    }

    override fun applicableAnnotations() = listOf(immutableAnnotation)

    override fun isApplicableAnnotationUsage(type: AnnotationUsageType): Boolean {
        return type in AnnotationUsageTypeCompat.setOf(
            AnnotationUsageTypeCompat.ASSIGNMENT,
            AnnotationUsageTypeCompat.METHOD_CALL)
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
        if (usage !is UExpression) return

        val scope: UElement = usage.getContainingUMethod() as? UElement
            ?: usage.getContainingUClass()
            ?: usage.getContainingUFile()
            ?: return

        val (instances, references, properties) =
            usage.getAvailableReturnValue()

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

            override fun field(assignment: UElement?, field: PsiField) {
                if (assignment != null && !checkIsImmutable(field)) {
                    reportFieldsToAssignment.add(field to assignment)
                }
            }

            override fun returns(expression: UReturnExpression) {
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

            override fun argument(call: UCallExpression, reference: UElement) {
                reportArgumentToFunction.add(reference to call)
            }
        })
    }

    private fun reportArgument(
        context: JavaContext,
        call: UCallExpression,
        reference: UElement
    ) {
        val method = call.resolve() ?: return
        val paramIndex = call.valueArguments.indexOf(reference)
        val param = method.parameters.getOrNull(paramIndex) as? PsiParameter ?: return
        val paramCls = (param.type as? PsiClassReferenceType)?.resolve() ?: return
        val refLocation = context.getLocation(param)
        if (refLocation.start == null || refLocation.end == null) return
        if (!context.checkIsAimClass(paramCls)) {
            return
        }
        val paramName = param.name
        if (!checkIsImmutable(param as PsiModifierListOwner)) {
            val refMsg = "Unable to pass an immutable object [${reference.asSourceString()}] " +
                "to a mutable parameter [${method.getSign()})"
            val callLocation = context.getCallLocation(call, true, true)
            val msg = "The method [${call.asSourceString()}] is called but this parameter " +
                "[$paramName] is mutable."
            val quickFix = LintFix.create()
                .name("Annotate parameter [${param.text}] with @Immutable")
                .replace()
                .range(refLocation)
                .with("@$immutableAnnotation ${param.text}")
                .reformat(true)
                .shortenNames()
                .build()

            context.report(ISSUE_WHISPER_MISSING_IMMUTABLE,
                call,
                refLocation.withSecondary(callLocation, refMsg),
                msg,
                quickFix)
        }
    }

    private fun PsiMethod.getSign(): String {
        val returnType = this.returnType?.canonicalText ?: "fun"
        val methodName = this.name
        val parameterList = this.parameters.joinToString {
            (it as? PsiParameter)?.text
                ?: it.name
                ?: ""
        }
        return "$returnType $methodName($parameterList)"
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
            if (checkReturnMethods.contains(getMethodName(element))) {
                var receiver: UExpression? = element.receiver
                while (receiver is UQualifiedReferenceExpression) {
                    receiver = receiver.receiver
                }
                if (receiver != null && checkIsImmutable(receiver)) {
                    return true
                }
            }
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

    private fun JavaContext.checkIsAimClass(cls: PsiClass): Boolean {
        return aimCls.any { this.evaluator.extendsClass(cls, it, false) }
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