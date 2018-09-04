package com.yy.mobile.whisper;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h2>标记被废弃且可被代替的方法。</h2>
 * 可以通过 {@link #replaceWith()} 指定代替的方法名，使用 IDE 的 Show Intention/ Quick Fix 快捷键，
 * 快速修复，使用新方法替换旧方法。
 */
@Documented
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.SOURCE)
public @interface DeprecatedBy {

    /**
     * 取代的新方法签名。
     * 可以使用 "%s" 来标记参数，把旧方法的参数用在新方法上。
     */
    String replaceWith();

    /**
     * 在 IDE 和 Lint Report 上提示的信息。
     */
    String message() default "";

    /**
     * 当新方法所在的类与废弃方法所在的类不是同一个，
     * 需要指定新方法所在的类。
     */
    String receiver() default "";

    /**
     * 错误等级。
     * {@link Level#Warning} 等级会在代码中标记警告。
     * {@link Level#Error} 等级会在 IDE 中标记错误，在 LintOptions#abortOnError 时中断编译。
     */
    Level level() default Level.Warning;

    enum Level {
        Warning, Error
    }
}
