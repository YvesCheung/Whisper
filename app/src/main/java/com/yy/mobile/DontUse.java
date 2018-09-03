package com.yy.mobile;

import com.yy.mobile.whisper.NeedWarning;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by 张宇 on 2018/8/15.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
@Inherited
@NeedWarning("do not use this method")
public @interface DontUse {
}