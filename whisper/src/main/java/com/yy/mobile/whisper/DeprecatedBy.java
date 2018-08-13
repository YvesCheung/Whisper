package com.yy.mobile.whisper;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by 张宇 on 2018/8/13.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
@Documented
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.SOURCE)
public @interface DeprecatedBy {

    String replaceWith();

    String message() default "";

    String receiver() default "";

    Level level() default Level.Warning;

    enum Level {
        Warning, Error
    }
}
