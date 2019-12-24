package com.yy.mobile.whisper

/**
 * 顾名思义，[DeprecatedBy] 和 [java.lang.Deprecated] 作用完全相同，
 * 唯一区别是，@DeprecatedBy 支持一键把废弃方法替换为新方法！
 *
 * 可以通过 [replaceWith] 指定代替的方法名，使用 IDE 的 Show Intention/ Quick Fix 快捷键，
 * 快速修复，使用新方法替换旧方法。
 *
 * 通常新增方法，都是在废弃的方法上增删可选的参数，或者调整参数的顺序防止与旧方法的签名一致。
 * 因此 [DeprecatedBy] 注解具有两种方法替换的特性：
 *
 * - 调整原方法的参数
 *
 * ```Java
 * @DeprecatedBy(
 *     replaceWith = "newMethod(%2$s, %1$s, null)",
 *     message = "This method is deprecated, use newMethod() instead.",
 *     level = DeprecatedBy.Level.Error
 * )
 * public void oldMethod(int param1, @NonNull String param2) {
 *     newMethod(param2, param1, null);
 * }
 *
 * public void newMethod(@NonNull String param2, int param1, @Nullable String param3) {
 *     //Do Something.
 * }
 * ```
 *
 * `replaceWith = "newMethod(%2$s, %1$s, null)"` 表示用 newMethod 来替换废弃方法，
 * 而且新方法第一个参数与旧方法的第二个参数相同，新方法第二个参数与旧方法第一个参数相同，新方法第三个参数为null。
 *
 * - 更改方法的接收者为其他单例
 *
 * ```Java
 * @DeprecatedBy(replaceWith = "newMethod(%s)", receiver = "deprecate.demo.ClassInstead")
 * public void oldMethod2(int param) {
 *     ClassInstead.newMethod(param);
 * }
 * ```
 *
 * `receiver = "deprecate.demo.ClassInstead"` 表示新方法是单例 `ClassInstead` 的静态方法，
 * 旧方法 `instance.oldMethod2(0)` 会被一键替换成 `ClassInstead.newMethod(0)`。
 *
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
