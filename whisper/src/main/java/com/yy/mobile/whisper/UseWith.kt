package com.yy.mobile.whisper


/**
 * 创建和销毁通常是成对出现的方法，如果开发者只调用了创建方法而忘记销毁，就会造成内存泄漏。
 * 注解 [UseWith] 的目的就是确保方法被成对调用。
 *
 * 可以注解在 `create`、`init`、`addListener` 等自定义方法上，
 * 注解接受一个字符串参数，代表对应的析构方法的名称。
 *
 * ```Java
 * public class SDK {
 *
 *     @UseWith("deInit")
 *     public void init() {}
 *
 *     public void deInit() {}
 *
 *     @UseWith("removeListener")
 *     public void addListener(OnStateChangeListener listener){}
 *
 *     public void removeListener(OnStateChangeListener listener){}
 * }
 * ```
 *
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
