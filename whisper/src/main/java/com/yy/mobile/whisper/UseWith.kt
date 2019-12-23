package com.yy.mobile.whisper


/**
 * @author YvesCheung
 * 2018/9/4.
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CONSTRUCTOR)
annotation class UseWith(val value: String)
