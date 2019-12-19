package com.yy.mobile.whisper;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author YvesCheung
 * 2018/8/13.
 *
 */
@Documented
@Target({ElementType.FIELD,
        ElementType.METHOD,
        ElementType.TYPE,
        ElementType.PARAMETER,
        ElementType.CONSTRUCTOR,
        ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.SOURCE)
@Inherited
public @interface NeedError {

    String value();
}
