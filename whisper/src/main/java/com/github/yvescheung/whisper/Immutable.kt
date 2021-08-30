package com.github.yvescheung.whisper

import java.util.*

/**
 * Java 缺少不可变集合的概念，导致对外暴露的集合往往需要拷贝一次，
 * 或者用 [Collections.unmodifiableCollection] 方法封装一次。
 *
 * [Immutable] 注解的目的是添加不可变集合的语义，使得被注解的 [java.util.List] /
 * [java.util.Queue] /[java.util.Map] / [java.util.Map.Entry] /
 * [java.util.Collection] / [java.util.Iterator] 无法被修改。
 *
 * ```Java
 * @Immutable
 * private final List<String> immutableList = new ArrayList<>();
 * ```
 *
 * 注解在集合类型的属性上，该属性的 [java.util.Collection.add] / [java.util.Collection.remove]
 * 等修改集合的方法的调用将会不合法。代码检阅将会扫描出集合的修改，并给予提示：
 * ```
 * src/aa/A.java:15: Error: you cannot invoke the [add(\"haha\")] method on an immutable object. [ImmutableObject]
 *   immutableList.add(\"haha\");
 *               ~~~~~~~~~~~~~~~~
 * src/aa/A.java:12: This reference is annotated by @Immutable
 * ```
 *
 * 若注解在方法上，方法返回的集合类型将无法被修改。
 * 如果方法返回的值已经被[Immutable]注解，那么方法也必须被[Immutable]注解，保证数据类型的一致性。
 *
 * ```Java
 * @Immutable
 * private List<String> getResult() {
 *     return immutableList;
 * }
 * ```
 *
 * 若注解在方法参数上，该参数的集合无法被修改。
 * 若注解的参数/变量赋值给另一个变量/属性，那么该变量/属性也需要被[Immutable]注解，保证数据类型的一致性。
 *
 * ```Java
 * @Immutable
 * private Set<Int> result;
 *
 * private void setResult(@Immutable Set<Int> result){
 *     this.result = result;
 * }
 * ```
 *
 * @author YvesCheung
 * 2018/9/8.
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.VALUE_PARAMETER)
annotation class Immutable
