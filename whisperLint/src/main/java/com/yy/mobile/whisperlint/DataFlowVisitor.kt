package com.yy.mobile.whisperlint

import com.android.tools.lint.detector.api.getMethodName
import com.android.tools.lint.detector.api.skipParentheses
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiLocalVariable
import com.intellij.psi.PsiVariable
import org.jetbrains.kotlin.asJava.elements.KtLightMethod
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UBlockExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UDeclarationsExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UExpressionList
import org.jetbrains.uast.UField
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.ULocalVariable
import org.jetbrains.uast.UPolyadicExpression
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.UReferenceExpression
import org.jetbrains.uast.UReturnExpression
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.UastCallKind.Companion.CONSTRUCTOR_CALL
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.getQualifiedParentOrThis
import org.jetbrains.uast.kotlin.AbstractKotlinUVariable
import org.jetbrains.uast.tryResolve
import org.jetbrains.uast.util.isAssignment
import org.jetbrains.uast.visitor.AbstractUastVisitor

/**
 * it's just like [com.android.tools.lint.checks.DataFlowAnalyzer],
 * but this one is more powerful and supports kotlin feature
 */
/** Helper class for analyzing data flow */
@Suppress("MemberVisibilityCanBePrivate")
abstract class DataFlowVisitor(
    private val initElements: Collection<UElement>,
    private val initReferences: Collection<PsiElement> = emptyList(),
    initProperties: Collection<String> = emptyList()
) : AbstractUastVisitor() {

    /** The instance being tracked is the receiver for a method call */
    open fun receiver(call: UCallExpression) {}

    /** The instance being tracked is being returned from this block */
    open fun returns(expression: UReturnExpression) {}

    /** The instance being tracked is being stored into a field */
    open fun field(field: UElement) {}

    /** The instance being tracked is being passed in a method call */
    open fun argument(
        call: UCallExpression,
        reference: UElement
    ) {
    }

    protected val references: MutableSet<PsiElement> = mutableSetOf()
    protected val instances: MutableSet<UElement> = mutableSetOf()
    protected val properties: MutableSet<String> = mutableSetOf()

    init {
        if (references.isEmpty()) {
            references.addAll(initReferences)
        }
        if (instances.isEmpty()) {
            instances.addAll(initElements)
            for (element in initElements) {
                if (element is UCallExpression) {
                    val parent = element.uastParent
                    if (parent is UQualifiedReferenceExpression && parent.selector == element) {
                        instances.add(parent)
                    }

                    val receiver = element.receiver
                    if (receiver == null || element.receiver.maybeIt()) {
                        val lambda = element.getParentOfType<ULambdaExpression>(ULambdaExpression::class.java, true)
                        val call = lambda?.uastParent as? UCallExpression
                        if (call != null && call.isKotlinScopingFunction()) {
                            instances.add(call)
                        }
                    }
                }
            }
        }
        if (properties.isEmpty()) {
            properties.addAll(initProperties)
        }
    }

    private fun includeNode(node: UExpression?): Boolean {
        node ?: return false

        fun isLightMethodButNotConstructor(psi: PsiElement): Boolean {
            if (psi is KtLightMethod) {
                if (node is UCallExpression) {
                    return node.kind != CONSTRUCTOR_CALL
                }
                return true
            }
            return false
        }

        if (instances.contains(node)) {
            return true
        } else {
            val resolved = node.tryResolve()
            if (resolved != null) {
                if (references.contains(resolved)) {
                    return true
                } else if (isLightMethodButNotConstructor(resolved) ||
                    resolved is PsiVariable) {
                    return properties.contains(resolved.text)
                }
            }
        }
        return false
    }

    override fun visitCallExpression(node: UCallExpression): Boolean {
        val receiver = node.receiver
        var matched = false
        if (receiver != null) {
            if (includeNode(receiver)) {
                matched = true
            } else if (receiver is UQualifiedReferenceExpression) {
                val rec = receiver.receiver
                val sel = receiver.selector as? UCallExpression
                if (sel != null) {
                    if (returnsSelf(sel) && includeNode(rec)) {
                        matched = true
                    } else if (sel.isKotlinScopingFunction() && includeNode(sel)) {
                        matched = true
                    }
                }
            }
        }

        val maybeIt = receiver.maybeIt()
        if (receiver == null || maybeIt) {
            val lambda: ULambdaExpression? = node.getParentOfType(ULambdaExpression::class.java, true)
            val call = lambda?.uastParent
            if (call is UCallExpression && call.isKotlinScopingFunction() &&
                // an expression which is one of the instances
                (instances.contains(node) ||
                    // a.apply and a is one of the instances
                    includeNode(call.receiver) ||
                    // an expression in lambda, which is one of the instances
                    instances.contains(lambda.body))
            ) {
                matched = true
            } else if (getMethodName(node) == "with") {
                val args = node.valueArguments
                if (args.size == 2 && includeNode(args[0]) &&
                    args[1] is ULambdaExpression) {
                    val body = (args[1] as ULambdaExpression).body
                    instances.add(body)
                    if (body is UBlockExpression) {
                        for (expression in body.expressions) {
                            instances.add(expression)
                        }
                    }
                }
            }
        }

        if (matched) {
            if (!initElements.contains(node)) {
                receiver(node)
            }
            if (returnsSelf(node)) {
                instances.add(node)
                val parent = node.uastParent as? UQualifiedReferenceExpression
                if (parent != null) {
                    instances.add(parent)
                    val parentParent = parent.uastParent as? UQualifiedReferenceExpression
                    val chained = parentParent?.selector
                    if (chained != null) {
                        instances.add(chained)
                    }
                }
            }

            if (node.isKotlinScopingFunction()) {
                (node.valueArguments.lastOrNull() as? ULambdaExpression)?.let {
                    val body = it.body
                    instances.add(body)
                    if (body is UBlockExpression) {
                        for (expression in body.expressions) {
                            instances.add(expression)
                        }
                    }
                }
            }
        }

        for (expression in node.valueArguments) {
            if (includeNode(expression)) {
                argument(node, expression)
            }
        }

        return super.visitCallExpression(node)
    }

    override fun afterVisitVariable(node: UVariable) {
        if (node is ULocalVariable || node is UField) {
            val initializer = node.uastInitializer
            if (includeNode(initializer)) {
                addVariableReference(node)
            }
        }
        //special case:
        //val field by lazy { ... }
        if (node is AbstractKotlinUVariable) {
            val exp = node.delegateExpression as? UCallExpression
            (exp?.valueArguments?.lastOrNull() as? ULambdaExpression)?.let { lambda ->
                val body = lambda.body
                if (includeNode(body)) {
                    addVariableReference(node)
                } else if (body is UBlockExpression) {
                    if (body.expressions.any { includeNode(it) }) {
                        addVariableReference(node)
                    }
                }
            }
        }
    }

    protected fun addVariableReference(node: UVariable) {
        if (node is ULocalVariable) {
            (node.sourcePsi as? PsiVariable)?.let { references.add(it) }
            (node.javaPsi as? PsiVariable)?.let { references.add(it) }
        } else if (node is UField) {
            properties.add(node.text)
        }
    }

    protected fun removeVariableReference(node: UVariable) {
        if (node is ULocalVariable) {
            (node.sourcePsi as? PsiVariable)?.let { references.remove(it) }
            (node.javaPsi as? PsiVariable)?.let { references.remove(it) }
        } else if (node is UField) {
            properties.remove(node.text)
        }
    }

    override fun visitBinaryExpression(node: UBinaryExpression): Boolean {
        if (node.isAssignment()) {
            // If we reassign one of the variables, clear it out
            val lhs = node.leftOperand.tryResolve()
            if (lhs != null && !initReferences.contains(lhs)) {
                when {
                    lhs is KtLightMethod -> properties.remove(lhs.text)
                    lhs is UVariable && !lhs.isFinal -> removeVariableReference(lhs)
                    lhs is PsiLocalVariable -> references.remove(lhs)
                }
            }
        }
        return super.visitBinaryExpression(node)
    }

    override fun afterVisitBinaryExpression(node: UBinaryExpression) {
        if (!node.isAssignment()) {
            return
        }

        val rhs = node.rightOperand
        if (includeNode(rhs)) {
            val lhs = node.leftOperand.tryResolve()
            when (lhs) {
                is KtLightMethod -> properties.add(lhs.text)
                is UVariable -> addVariableReference(lhs)
                is PsiLocalVariable -> references.add(lhs)
                is PsiField -> field(rhs)
            }
        }
    }

    override fun afterVisitReturnExpression(node: UReturnExpression) {
        val returnValue = node.returnExpression
        if (returnValue != null) {
            if (instances.contains(returnValue)) {
                returns(node)
            } else if (returnValue is UReferenceExpression) {
                val resolved = returnValue.resolve()
                if (resolved != null && references.contains(resolved)) {
                    returns(node)
                }
            }
        }
    }

    /**
     * Tries to guess whether the given method call returns self.
     * This is intended to be able to tell that in a constructor
     * call chain foo().bar().baz() is still invoking methods on the
     * foo instance.
     */
    open fun returnsSelf(call: UCallExpression): Boolean {
        if (call.isKotlinReturnSelfFunction()) {
            return true
        }
        val resolvedCall = call.resolve() ?: return false
        return (call.returnType as? PsiClassType)?.resolve() == resolvedCall.containingClass
    }

    companion object {
        /** Returns the variable the expression is assigned to, if any  */
        fun getVariableElement(rhs: UCallExpression): PsiVariable? {
            return getVariableElement(rhs, false, false)
        }

        fun getVariableElement(
            rhs: UCallExpression,
            allowChainedCalls: Boolean,
            allowFields: Boolean
        ): PsiVariable? {
            var parent = skipParentheses(rhs.getQualifiedParentOrThis().uastParent)

            // Handle some types of chained calls; e.g. you might have
            //    var = prefs.edit().put(key,value)
            // and here we want to skip past the put call
            if (allowChainedCalls) {
                while (true) {
                    if (parent is UQualifiedReferenceExpression) {
                        val parentParent = skipParentheses(parent.uastParent)
                        if (parentParent is UQualifiedReferenceExpression) {
                            parent = skipParentheses(parentParent.uastParent)
                        } else if (parentParent is UVariable || parentParent is UPolyadicExpression) {
                            parent = parentParent
                            break
                        } else {
                            break
                        }
                    } else {
                        break
                    }
                }
            }

            if (parent != null && parent.isAssignment()) {
                val assignment = parent as UBinaryExpression
                val lhs = assignment.leftOperand
                if (lhs is UReferenceExpression) {
                    val resolved = lhs.resolve()
                    if (resolved is PsiVariable && (allowFields || resolved !is PsiField)) {
                        // e.g. local variable, parameter - but not a field
                        return resolved
                    }
                }
            } else if (parent is UVariable && (allowFields || parent !is UField)) {
                // Handle elvis operators in Kotlin. A statement like this:
                //   val transaction = f.beginTransaction() ?: return
                // is turned into
                //   var transaction: android.app.FragmentTransaction = elvis {
                //       @org.jetbrains.annotations.NotNull var var8633f9d5: android.app.FragmentTransaction = f.beginTransaction()
                //       if (var8633f9d5 != null) var8633f9d5 else return
                //   }
                // and here we want to record "transaction", not "var8633f9d5", as the variable
                // to track.
                if (parent.uastParent is UDeclarationsExpression &&
                    parent.uastParent!!.uastParent is UExpressionList
                ) {
                    val exp = parent.uastParent!!.uastParent as UExpressionList
                    val kind = exp.kind
                    if (kind.name == "elvis" && exp.uastParent is UVariable) {
                        parent = exp.uastParent
                    }
                }

                return (parent as UVariable).psi
            }

            return null
        }
    }
}