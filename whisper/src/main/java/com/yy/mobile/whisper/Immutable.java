package com.yy.mobile.whisper;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by 张宇 on 2018/9/8.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
@Documented
@Target({ElementType.FIELD,
        ElementType.METHOD,
        ElementType.LOCAL_VARIABLE,
        ElementType.PARAMETER})
@Retention(RetentionPolicy.SOURCE)
public @interface Immutable {
}
