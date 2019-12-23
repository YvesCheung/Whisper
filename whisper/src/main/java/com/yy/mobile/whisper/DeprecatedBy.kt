package com.yy.mobile.whisper

/**
 * 标记被废弃且可被代替的方法。
 * 可以通过 [.replaceWith] 指定代替的方法名，使用 IDE 的 Show Intention/ Quick Fix 快捷键，
 * 快速修复，使用新方法替换旧方法。
 *
 * @author YvesCheung
 * 2018/9/8
 */
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CONSTRUCTOR)
annotation class DeprecatedBy(
    /**
     * 取代的新方法签名。
     * 可以使用 "%s" 来标记参数，把旧方法的参数用在新方法上。
     */
    val replaceWith: String,
    /**
     * 在 IDE 和 Lint Report 上提示的信息。
     */
    val message: String = "",
    /**
     * 当新方法所在的类与废弃方法所在的类不是同一个，
     * 需要指定新方法所在的类。
     */
    val receiver: String = "",
    /**
     * 错误等级。
     * [Level.Warning] 等级会在代码中标记警告。
     * [Level.Error] 等级会在 IDE 中标记错误，在 LintOptions#abortOnError 时中断编译。
     */
    val level: Level = Level.Warning
) {

    enum class Level {
        Warning, Error
    }
}
