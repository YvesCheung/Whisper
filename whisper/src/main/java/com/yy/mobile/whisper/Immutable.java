package com.yy.mobile.whisper;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author YvesCheung
 * 2018/9/8.
 *
 */
@Documented
@Target({ElementType.FIELD,
        ElementType.METHOD,
        ElementType.LOCAL_VARIABLE,
        ElementType.PARAMETER})
@Retention(RetentionPolicy.SOURCE)
public @interface Immutable {
}
