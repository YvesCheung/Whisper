package com.yy.mobile.whisper

import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.LOCAL_VARIABLE
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.PROPERTY_SETTER
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

/**
 * Denotes that the annotated element should be a float or double in the given range
 *
 * @author YvesCheung
 * 2019-12-24
 */
@MustBeDocumented
@Retention(SOURCE)
@Target(FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, VALUE_PARAMETER, FIELD, LOCAL_VARIABLE, ANNOTATION_CLASS)
annotation class FloatRange(
    /** Smallest value. Whether it is inclusive or not is determined
     * by [.fromInclusive]  */
    val from: Float = java.lang.Float.NEGATIVE_INFINITY,
    /** Largest value. Whether it is inclusive or not is determined
     * by [.toInclusive]  */
    val to: Float = java.lang.Float.POSITIVE_INFINITY,
    /** Whether the from value is included in the range  */
    val fromInclusive: Boolean = true,
    /** Whether the to value is included in the range  */
    val toInclusive: Boolean = true
)