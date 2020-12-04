package com.yy.mobile.whisper

import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.LOCAL_VARIABLE
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

/**
 * Denotes that the annotated element should be an int in the given range.
 *
 * @author YvesCheung
 * 2019-12-24
 */
@MustBeDocumented
@Retention(SOURCE)
@Target(FUNCTION, VALUE_PARAMETER, FIELD, LOCAL_VARIABLE)
annotation class IntRange(val from: Int = Int.MIN_VALUE, val to: Int = Int.MAX_VALUE)