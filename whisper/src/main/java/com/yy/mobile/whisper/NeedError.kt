package com.yy.mobile.whisper

/**
 * [NeedError]可以作用在方法/属性的定义上。添加[NeedError]注解后，IDE会在方法/属性的使用上作出提示。
 * 比如在一个属性上标记上注解：
 *
 * ```Java
 *  public class A {
 *
 *      @NeedError("这个变量有祖传的BUG")
 *      int field = 3;
 *  }
 * ```
 *
 * 若在类B中有对注解属性的引用：
 *
 * ```Java
 *  public class B {
 *
 *      private void func(){
 *          int a = field;
 *      }
 *  }
 * ```
 *
 * 那么，代码检阅将会在类B上扫描出这段代码，并给予提示：
 *
 * ```
 * src/com/whisper/mobile/B.java:11: Error: 这个变量有祖传的BUG [WhisperError]
 *      a = field;
 *        ~~~~~~~~
 * ```
 *
 * @see NeedError
 * @see NeedWarning
 *
 * @author YvesCheung
 * 2018/8/13.
 */
@MustBeDocumented
@Target(AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CLASS,
    AnnotationTarget.FILE,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class NeedError(val value: String)
