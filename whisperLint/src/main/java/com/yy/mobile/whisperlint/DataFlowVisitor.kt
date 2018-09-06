package com.yy.mobile.whisperlint

import com.android.tools.lint.detector.api.getMethodName
import com.android.tools.lint.detector.api.skipParentheses
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiLocalVariable
import com.intellij.psi.PsiVariable
import org.jetbrains.kotlin.asJava.elements.KtLightIdentifier
import org.jetbrains.kotlin.asJava.elements.KtLightMethodImpl
import org.jetbrains.kotlin.psi.KtProperty
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
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.getQualifiedParentOrThis
import org.jetbrains.uast.kotlin.psi.UastKotlinPsiVariable
import org.jetbrains.uast.tryResolve
import org.jetbrains.uast.util.isAssignment
import org.jetbrains.uast.visitor.AbstractUastVisitor

/**
 * it's just copy from [com.android.tools.lint.checks.DataFlowAnalyzer].
 * However,
 * 1. [initialReferences] should not a `Collection<PsiVariable>` but `Collection<PsiElement>`
 * 2. [returnSelf] should include Kotlin function like `apply` `also` `takeIf`
 */
/** Helper class for analyzing data flow */
@Suppress("MemberVisibilityCanBePrivate")
abstract class DataFlowVisitor(
    val initial: Collection<UElement>,
    initialReferences: Collection<PsiElement> = emptyList()
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
            references.addAll(initialReferences)
        }
        if (instances.isEmpty()) {
            instances.addAll(initial)
            for (element in initial) {
                if (element is UCallExpression) {
                    val parent = element.uastParent
                    if (parent is UQualifiedReferenceExpression && parent.selector == element) {
                        instances.add(parent)
                    }
                }
            }
        }
    }

    override fun visitCallExpression(node: UCallExpression): Boolean {
        val receiver = node.receiver
        var matched = false
        if (receiver != null) {
            if (instances.contains(receiver)) {
                matched = true
            } else {
                val resolved = receiver.tryResolve()
                if (resolved != null) {
                    if (references.contains(resolved)) {
                        matched = true
                    } else {
                        (resolved as? KtLightMethodImpl)?.let { maybeProperty ->
                            val id = maybeProperty.nameIdentifier as? KtLightIdentifier
                            (id?.text ?: id?.name)?.let { name ->
                                if (properties.contains(name)) {
                                    matched = true
                                }
                            }
                        }
                        (resolved as? UastKotlinPsiVariable)?.let { maybeProperty ->
                            if (maybeProperty.psiParent is KtProperty &&
                                properties.contains(maybeProperty.name)) {
                                matched = true
                            }
                        }
                    }
                }
            }
            System.out.println("rec = ${receiver.asSourceString()} match = $matched")
        }

        val maybeIt = receiver.maybeIt()
        if (receiver == null || maybeIt) {
            val lambda: ULambdaExpression? =
                if (maybeIt) {
                    node.uastParent?.uastParent as? ULambdaExpression
                        ?: node.uastParent?.uastParent?.uastParent as? ULambdaExpression
                } else {
                    node.uastParent as? ULambdaExpression
                        ?: node.uastParent?.uastParent as? ULambdaExpression
                }

            if (lambda != null && lambda.uastParent is UCallExpression &&
                isKotlinScopingFunction(lambda.uastParent as UCallExpression)) {
                System.out.println("lambda = ${lambda.uastParent?.asSourceString()}")
                System.out.println("instance = ${instances.map { it.asSourceString() }} " +
                    "contain = ${instances.contains(node)} " +
                    "containP = ${instances.contains(node.uastParent)}")
                if (instances.contains(node) || instances.contains(node.uastParent)) {
                    matched = true
                }
            } else if (getMethodName(node) == "with") {
                val args = node.valueArguments
                if (args.size == 2 && instances.contains(args[0]) &&
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
            if (!initial.contains(node)) {
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

            if (isKotlinScopingFunction(node)) {
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
            if (instances.contains(expression)) {
                argument(node, expression)
            } else if (expression is UReferenceExpression) {
                val resolved = expression.resolve()

                if (resolved != null && references.contains(resolved)) {
                    argument(node, expression)
                    break
                }
            }
        }

        return super.visitCallExpression(node)
    }

    private fun UExpression?.maybeIt(): Boolean {
        val receiver = this ?: return false
        return receiver is USimpleNameReferenceExpression &&
            receiver.tryResolve() == null
    }

    private fun isKotlinReturnSelfFunction(node: UCallExpression): Boolean {
        val methodName = getMethodName(node).apply { }
        return methodName == "apply" ||
            methodName == "also" ||
            methodName == "takeIf" ||
            methodName == "takeUnless"
    }

    private fun isKotlinScopingFunction(node: UCallExpression): Boolean {
        val methodName = getMethodName(node)
        return methodName == "apply" ||
            methodName == "run" ||
            methodName == "with" ||
            methodName == "also" ||
            methodName == "let" ||
            methodName == "takeIf" ||
            methodName == "takeUnless"
    }

    override fun afterVisitVariable(node: UVariable) {
        if (node is ULocalVariable || node is UField) {
            val initializer = node.uastInitializer
            if (initializer != null) {
                if (instances.contains(initializer)) {
                    // Instance is stored in a variable
                    addVariableReference(node)
                } else if (initializer is UReferenceExpression) {
                    val resolved = initializer.resolve()
                    if (resolved != null && references.contains(resolved)) {
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
            properties.add(node.name)
        }
        System.out.println("addRef ${node.sourcePsi?.let { it::class.java }} ${node.javaPsi} " +
            "curRef = ${references.joinToString { it.text }}")
    }

    override fun afterVisitBinaryExpression(node: UBinaryExpression) {
        if (!node.isAssignment()) {
            return
        }

        // TEMPORARILY DISABLED; see testDatabaseCursorReassignment
        // This can result in some false positives right now. Play it
        // safe instead.
        var clearLhs = false

        val rhs = node.rightOperand
        if (instances.contains(rhs)) {
            val lhs = node.leftOperand.tryResolve()
            when (lhs) {
                is UVariable -> addVariableReference(lhs)
                is PsiLocalVariable -> references.add(lhs)
                is PsiField -> field(rhs)
            }
        } else if (rhs is UReferenceExpression) {
            val resolved = rhs.resolve()
            if (resolved != null && references.contains(resolved)) {
                clearLhs = false
                val lhs = node.leftOperand.tryResolve()
                when (lhs) {
                    is UVariable -> addVariableReference(lhs)
                    is PsiLocalVariable -> references.add(lhs)
                    is PsiField -> field(rhs)
                }
            }
        }

        if (clearLhs) {
            // If we reassign one of the variables, clear it out
            val lhs = node.leftOperand.tryResolve()
            if (lhs != null && lhs != initial && references.contains(lhs)) {
                references.remove(lhs)
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
        if (isKotlinReturnSelfFunction(call)) {
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