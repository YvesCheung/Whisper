package com.github.yvescheung.whisper

import org.jetbrains.annotations.TestOnly

/**
 * Java语言没有友元函数的概念。
 *
 * “包级私有”的可见性，调用方可以通过改包名来实现访问目的，Kotlin语言更是允许包名和文件路径不一致。
 * Kotlin的“模块级(internal)”的可见性也有利有弊。
 * [Hide] 注解的目的是为了表达友元的概念，防止个别方法破坏类的封装性。
 *
 * 比如常见地我们希望实现一个仅用于单元测试的方法，该方法会直接hook住关键的业务逻辑。
 * [TestOnly] 注解不会影响该方法的可见性，其他开发者可调用该方法而导致错误。
 *
 * [Hide] 注解参数可以指定其他类的全限定符或者简单类名。只有满足参数指定的类，才可以访问注解的方法。
 *
 * ```Java
 * public class Config {
 *
 *     @Hide(friend = {"com.mobile.debug.SettingActivity"})
 *     public void setDebuggable(boolean enable) {}
 * }
 * ```
 *
 * @author YvesCheung
 * 2018/8/13.
 */
@MustBeDocumented
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.SOURCE)
annotation class Hide(val friend: Array<String>)
