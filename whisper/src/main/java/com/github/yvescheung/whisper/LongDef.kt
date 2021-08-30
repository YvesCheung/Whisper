package com.github.yvescheung.whisper

/**
 * @author YvesCheung
 * 2019-12-25
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FIELD,
    AnnotationTarget.LOCAL_VARIABLE)
annotation class LongDef(vararg val value: Long)