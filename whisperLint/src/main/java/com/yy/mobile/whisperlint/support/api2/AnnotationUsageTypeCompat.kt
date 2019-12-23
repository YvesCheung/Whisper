package com.yy.mobile.whisperlint.support.api2

import com.android.tools.lint.detector.api.AnnotationUsageType
import com.yy.mobile.whisperlint.support.VersionChecker

/**
 * compat android gradle plugin <= 3.2
 *
 * @author YvesCheung
 * 2019-12-23
 */
enum class AnnotationUsageTypeCompat {

    /** A call to a method where the method it self was annotated */
    METHOD_CALL,

    /** A reference to a member in a class where the class was annotated */
    METHOD_CALL_CLASS,

    /** A reference to a member in a package where the package was annotated */
    METHOD_CALL_PACKAGE,

    /** An argument to a method call where the corresponding parameter was annotated */
    METHOD_CALL_PARAMETER,

    /** An argument to an annotation where the annotation parameter has been annotated */
    ANNOTATION_REFERENCE,

    /** A return from a method that was annotated */
    METHOD_RETURN,

    /** A variable whose declaration was annotated */
    VARIABLE_REFERENCE,

    /** The right hand side of an assignment (or variable/field declaration) where the
     * left hand side was annotated */
    ASSIGNMENT,

    /**
     * An annotated element is combined with this element in a binary expression
     * (such as +, -, >, ==, != etc.). Note that [EQUALITY] is a special case.
     */
    BINARY,

    /** An annotated element is compared for equality or not equality */
    EQUALITY,

    /** A class extends or implements an annotated element */
    EXTENDS,

    /** An annotated field is referenced */
    FIELD_REFERENCE;

    companion object {

        fun setOf(vararg type: AnnotationUsageTypeCompat): Set<AnnotationUsageType> {
            val origin = type.toMutableSet()
            if (VersionChecker.envVersion() <= 2) {
                origin -= FIELD_REFERENCE
            }
            return mutableSetOf<AnnotationUsageType>().also { set ->
                origin.mapTo(set) { type ->
                    AnnotationUsageType.valueOf(type.name)
                }
            }
        }
    }
}