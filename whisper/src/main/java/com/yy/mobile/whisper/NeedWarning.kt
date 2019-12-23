package com.yy.mobile.whisper

/**
 * @author YvesCheung
 * 2018/8/11.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CLASS,
    AnnotationTarget.FILE,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.ANNOTATION_CLASS)
@MustBeDocumented
annotation class NeedWarning(val value: String)
