package com.github.yvescheung.whisper

import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.PROPERTY_SETTER
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

/**
 * Indicate the parameter is passed from outside
 *
 * ```kotlin
 * class Fragment {
 *
 *     @Input
 *     var callback: OnClickListener? = null
 *
 *     //....
 * }
 * ```
 *
 * @see Output
 *
 * @author YvesCheung
 * 2020/11/11
 */
@MustBeDocumented
@Target(FIELD, VALUE_PARAMETER, PROPERTY_GETTER, PROPERTY_SETTER)
@Retention(SOURCE)
annotation class Input