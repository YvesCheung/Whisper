# Whisper

> 一套基于Android Lint的代码检阅规则，用于对指定注解的代码进行提示和建议

![hello whisper][1]

**Whisper** 是一套针对注解的代码检阅规则，作用于Android静态代码扫描，可以给开发人员在编写代码时提供代码提示和修改建议。**Whisper** 基于 [**Lint**][2] 开发，**Lint** 天然集成在 **Android Studio** 等IDE中，IDE 可以对命中规则的代码进行UI提示。

**Whisper** 具有多种注解，每种注解有不同的提示作用——

## @NeedInfo
不要误以为自己能写出“自解释”的代码！充足的注释和文档可以使代码维护变得更简单。

 `NeedInfo` 注解提供了一种给代码 **“强提示”** 的效果。`@NeedInfo` 注解的目标可以是变量、方法或者方法参数，IDE会对方法、属性的使用方进行代码提示。 `@NeedInfo` 接受一个字符串参数，内容就是代码提示的内容。

例如，在方法 `method1` 上添加注解 `@NeedInfo("千万不要修改这个方法，有坑！")`，当开发者的光标悬停在 `method1` 的方法调用上，IDE就会给出 *“千万不要修改这个方法，有坑！”* 的提示。

![NeedInfo][3]

`@NeedWarning` `@NeedError` 是与 `@NeedInfo` 相似的注解，唯一区别是代码提示的严重等级不同。不同的严重等级会在IDE上有不同的表现，具体样式可以在IDE中自定义设置。

## @UseWith
创建和销毁通常是成对出现的方法，如果开发者只调用了创建方法而忘记销毁，就会造成内存泄漏。
注解 `@UseWith` 的目的就是确保方法被成对调用。可以注解在 `create`、`init`、`addListener` 等自定义方法上，注解接受一个字符串参数，代表对应的析构方法的名称。

![UseWith][4]

## @Hide
Java语言没有友元函数的概念。“包级私有”的可见性，调用方可以通过改包名来实现访问目的，Kotlin语言更是允许包名和文件路径不一致。Kotlin的“模块级(internal)”的可见性也有利有弊。

`@Hide` 注解的目的是为了表达友元的概念，防止个别方法破坏类的封装性。
比如常见地我们希望实现一个仅用于单元测试的方法，该方法会直接hook住关键的业务逻辑。`@org.jetbrains.annotations.TestOnly` 注解不会影响该方法的可见性，其他开发者可调用该方法而导致错误。

`@Hide` 注解参数可以指定其他类的全限定符或者简单类名。只有满足参数指定的类，才可以访问注解的方法。

![Hide][5]

如上图，类 `ATest` 和 类 `BTest`同时访问类 `A`的方法，但只有 `ATest` 允许访问。

## @DeprecatedBy
顾名思义，`@DeprecatedBy` 和 `@java.lang.Deprecated` 作用完全相同，唯一区别是，`@DeprecatedBy` 支持一键把废弃方法替换为新方法！

![DeprecatedBy][6]

当Kotlin语言在 `@kotlin.Deprecated` 注解中新增了 `replaceWith` 字段的时候，我就以为会做快捷替换的功能，结果没有。因此才添加了这个注解。

通常新增方法，都是在废弃的方法上增删可选的参数，或者调整参数的顺序防止与旧方法的签名一致。因此 `@DeprecatedBy` 注解具有两种方法替换的特性：

- 调整原方法的参数

    ```Java
    @DeprecatedBy(
            replaceWith = "newMethod(%2$s, %1$s, null)",
            message = "This method is deprecated, use newMethod() instead.",
            level = DeprecatedBy.Level.Error
    )
    public void oldMethod(int param1, @NonNull String param2) {
        newMethod(param2, param1, null);
    }

    public void newMethod(@NonNull String param2, int param1, @Nullable String param3) {
        //Do Something.
    }
    ```
    
     `replaceWith = "newMethod(%2$s, %1$s, null)"` 表示用 `newMethod` 来替换废弃方法，而且新方法第一个参数与旧方法的第二个参数相同，新方法第二个参数与旧方法第一个参数相同，新方法第三个参数为null。
    
- 更改方法的接收者为其他单例

    ```Java
    @DeprecatedBy(replaceWith = "newMethod(%s)", receiver = "deprecate.demo.ClassInstead")
    public void oldMethod2(int param) {
        ClassInstead.newMethod(param);
    }
    ```
    
    `receiver = "deprecate.demo.ClassInstead"` 表示新方法是单例 `ClassInstead` 的静态方法，旧方法 `instance.oldMethod2(0)` 会被一键替换成 `ClassInstead.newMethod(0)`。
    
## @Immutable
Java 缺少不可变集合的概念，导致对外暴露的集合往往需要拷贝一次，或者用 `Collections.unmodifiableCollection` 方法封装一次。

`@Immutable` 注解的目的是添加不可变集合的语义，使得被注解的 `List` / `Queue` / `Map` / `Map.Entry` / `Collection` / `Iterator` 无法被修改。

![Immutable][7]

Kotlin 中由于已经有不可变集合的概念，所以不需要这个注解。

# 安装
1. 根目录 `build.gradle` 配置仓库

    ```groovy
    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
    ```
    
2. 使用注解的模块添加依赖 

    ```groovy
    dependencies {
	    implementation 'com.github.YvesCheung:Whisper:$VERSION'
	}
	```
	
    其中 *$VERSION* 为 [![](https://jitpack.io/v/YvesCheung/Whisper.svg)](https://jitpack.io/#YvesCheung/Whisper)
    
    

  [1]: https://raw.githubusercontent.com/YvesCheung/Whisper/master/art/hello-whisper.jpg
  [2]: https://developer.android.com/studio/write/lint?hl=zh-CN
  [3]: https://raw.githubusercontent.com/YvesCheung/Whisper/master/art/%40NeedInfo.gif
  [4]: https://raw.githubusercontent.com/YvesCheung/Whisper/master/art/@UseWith.gif
  [5]: https://raw.githubusercontent.com/YvesCheung/Whisper/master/art/@Hide.gif
  [6]: https://raw.githubusercontent.com/YvesCheung/Whisper/master/art/@DeprecateBy.gif
  [7]: https://raw.githubusercontent.com/YvesCheung/Whisper/master/art/@Immutable.gif
