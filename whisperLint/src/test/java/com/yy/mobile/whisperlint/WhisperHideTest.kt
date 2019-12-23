package com.yy.mobile.whisperlint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask
import org.junit.Test

/**
 * @author YvesCheung
 * 2018/9/3.
 *
 */
class WhisperHideTest {

    private val hideAnnotation: TestFile = java("""
                package com.yy.mobile.whisper;

                import java.lang.annotation.Documented;
                import java.lang.annotation.ElementType;
                import java.lang.annotation.Retention;
                import java.lang.annotation.RetentionPolicy;
                import java.lang.annotation.Target;

                @Documented
                @Target({ElementType.FIELD,
                        ElementType.METHOD,
                        ElementType.CONSTRUCTOR})
                @Retention(RetentionPolicy.SOURCE)
                public @interface Hide {

                    String[] friend() default {};
                }
    """.trimIndent())

    @Test
    fun `Check Java @Hide method use in itSelf or friend class`() {

        TestLintTask.lint().files(
            hideAnnotation,
            java("""
                package cc;
                import com.yy.mobile.whisper.Hide;

                public class TheClassShouldBeHide {

                    public TheClassShouldBeHide() {
                        method();
                    }

                    @Hide(friend = {"MainActivity"})
                    public void method() {
                        int a = 3 + 4;
                    }
                }
            """.trimIndent()),
            java("""
                package cc.aa;
                import cc.TheClassShouldBeHide;

                public class MainActivity {

                    private TheClassShouldBeHide field = new TheClassShouldBeHide();

                    MainActivity(){
                        field.method();
                    }
                }
            """.trimIndent()),
            kotlin("""
                package cc

                class MainActivity : android.support.v7.app.AppCompatActivity {

                    override fun onCreate(){
                        TheClassShouldBeHide().method()
                    }
                }
            """.trimIndent()))
            .detector(WhisperHideDetector())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun `Check Kotlin @Hide method use in itSelf or friend class`() {

        TestLintTask.lint().files(
            hideAnnotation,
            kotlin("""
                package cc

                import com.yy.mobile.whisper.Hide

                class TheClassShouldBeHide {

                    init {
                        method();
                    }

                    fun method2() = method()

                    @Hide(friend = {"MainActivity"})
                    fun method() {
                        val a = 3 + 4
                    }
                }
            """.trimIndent()),
            java("""
                package cc.aa;
                import cc.TheClassShouldBeHide;

                public class MainActivity {

                    private TheClassShouldBeHide field = new TheClassShouldBeHide();

                    MainActivity(){

                        field.method();
                    }
                }
            """.trimIndent()),
            kotlin("""
                package cc

                class MainActivity : android.support.v7.app.AppCompatActivity {

                    override fun onCreate(){
                        TheClassShouldBeHide().method()
                    }
                }
            """.trimIndent()))
            .detector(WhisperHideDetector())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun `Check @Hide method in unfriendly class`() {
        TestLintTask.lint().files(
            hideAnnotation,
            java("""
                package cc;

                import com.yy.mobile.whisper.Hide;

                public class TheClassShouldBeHide {

                    public TheClassShouldBeHide() {
                        method();
                    }

                    @Hide(friend = {"cc.NewActivity"})
                    public void method() {
                        int a = 3 + 4;
                    }
                }
            """.trimIndent()),
            java("""
                package cc.aa;
                import cc.TheClassShouldBeHide;

                public class MainActivity {

                    private TheClassShouldBeHide field = new TheClassShouldBeHide();

                    MainActivity(){
                        field.method();
                    }
                }
            """.trimIndent()),
            kotlin("""
                package cc

                class MainActivity : android.support.v7.app.AppCompatActivity {

                    override fun onCreate(){
                        TheClassShouldBeHide().method()
                    }
                }
            """.trimIndent()))
            .detector(WhisperHideDetector())
            .run()
            .expect("src/cc/aa/MainActivity.java:9: Error: Methods that can only be accessed in [cc.NewActivity, cc.TheClassShouldBeHide] [HideMember]\n" +
                "        field.method();\n" +
                "              ~~~~~~\n" +
                "src/cc/MainActivity.kt:6: Error: Methods that can only be accessed in [cc.NewActivity, cc.TheClassShouldBeHide] [HideMember]\n" +
                "        TheClassShouldBeHide().method()\n" +
                "                               ~~~~~~\n" +
                "2 errors, 0 warnings")
    }

    @Test
    fun `Check @Hide method with qualified name in unfriendly class`() {
        TestLintTask.lint().files(
            hideAnnotation,
            java("""
                package cc;
                import com.yy.mobile.whisper.Hide;

                public class TheClassShouldBeHide {

                    public TheClassShouldBeHide() {
                        method();
                    }

                    @Hide(friend = {"cc.bb.NewActivity"})
                    public void method() {
                        int a = 3 + 4;
                    }
                }
            """.trimIndent()),
            java("""
                package cc.aa;
                import cc.TheClassShouldBeHide;

                public class NewActivity {

                    private TheClassShouldBeHide field = new TheClassShouldBeHide();

                    MainActivity(){
                        field.method();
                    }
                }
            """.trimIndent()),
            kotlin("""
                package cc.bb

                class NewActivity : android.support.v7.app.AppCompatActivity() {

                    override fun onCreate(){
                        TheClassShouldBeHide().method()
                    }
                }
            """.trimIndent()))
            .detector(WhisperHideDetector())
            .run()
            .expect("src/cc/aa/NewActivity.java:9: Error: Methods that can only be accessed in [cc.bb.NewActivity, cc.TheClassShouldBeHide] [HideMember]\n" +
                "        field.method();\n" +
                "              ~~~~~~\n" +
                "1 errors, 0 warnings")
    }

    @Test
    fun `Check static @Hide method in unfriendly class`() {
        TestLintTask.lint().files(
            hideAnnotation,
            java("""
                package cc;
                import com.yy.mobile.whisper.Hide;

                public class TheClassShouldBeHide {

                    public TheClassShouldBeHide() {
                        method();
                    }

                    @Hide(friend = {"cc.NewActivity"})
                    public static void method() {
                        int a = 3 + 4;
                    }
                }
            """.trimIndent()),
            java("""
                package cc;

                public class NewActivity {

                    MainActivity(){
                        TheClassShouldBeHide.method();
                    }
                }
            """.trimIndent()),
            java("""
                package ccc;
                import cc.TheClassShouldBeHide;

                public class NewActivity {

                    MainActivity(){
                        TheClassShouldBeHide.method();
                    }
                }
            """.trimIndent()),
            kotlin("""
                package cc.bb

                import cc.TheClassShouldBeHide

                class NewActivity : android.support.v7.app.AppCompatActivity {
                    override fun onCreate(){
                        TheClassShouldBeHide.method()
                    }
                }
            """.trimIndent()))
            .detector(WhisperHideDetector())
            .run()
            .expect("src/ccc/NewActivity.java:7: Error: Methods that can only be accessed in [cc.NewActivity, cc.TheClassShouldBeHide] [HideMember]\n" +
                "        TheClassShouldBeHide.method();\n" +
                "                             ~~~~~~\n" +
                "src/cc/bb/NewActivity.kt:7: Error: Methods that can only be accessed in [cc.NewActivity, cc.TheClassShouldBeHide] [HideMember]\n" +
                "        TheClassShouldBeHide.method()\n" +
                "                             ~~~~~~\n" +
                "2 errors, 0 warnings")
    }

    @Test
    fun `Check @Hide method in inner unfriendly class`() {
        TestLintTask.lint().files(
            hideAnnotation,
            kotlin("""
                package aa.bb.cc
                import com.yy.mobile.whisper.Hide

                class TheClassShouldBeHide {

                    fun method2() = method()

                    @Hide(friend = ["aa.dd.NewActivity"])
                    fun method(): Int = 3 + 4
                }
            """.trimIndent()),
            java("""
                package aa.dd;
                import aa.bb.cc.*;

                public class NewActivity {

                    public class InnerNewActivity {

                        private TheClassShouldBeHide field = new TheClassShouldBeHide();

                        public void method3() {
                            field.method();
                        }

                        private class InnerInnerNewActivity {

                            public void method4() {
                                field.method();
                            }
                        }
                    }
                }
            """.trimIndent()))
            .detector(WhisperHideDetector())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun `Check @Hide method in inner static unfriendly class`() {
        TestLintTask.lint().files(
            hideAnnotation,
            kotlin("""
                package aa.bb.cc
                import com.yy.mobile.whisper.Hide

                class TheClassShouldBeHide {

                    fun method2() = method()

                    @Hide(friend = ["aa.dd.NewActivity", "aa.dd.ee.MainActivity"])
                    fun method(): Int = 3 + 4
                }
            """.trimIndent()),
            java("""
                package aa.dd;
                import aa.bb.cc.*;

                public class NewActivity {

                    public class InnerNewActivity {

                        private TheClassShouldBeHide field = new TheClassShouldBeHide();

                        public void method3() {
                            field.method();
                        }

                        private static class InnerInnerNewActivity {

                            public void method4() {
                                field.method();
                            }
                        }
                    }
                }
            """.trimIndent()),
            kotlin("""
                package aa.dd.ee
                import aa.bb.cc.TheClassShouldBeHide;

                class MainActivity : android.support.v7.app.AppCompatActivity() {

                    override fun onCreate() {
                        TheClassShouldBeHide().method2()
                        TheClassShouldBeHide().method()
                    }

                    class NewActivity {

                        init {
                            TheClassShouldBeHide().method2()
                            TheClassShouldBeHide().method()
                        }
                    }
                }
            """.trimIndent()))
            .detector(WhisperHideDetector())
            .run()
            .expect("src/aa/dd/ee/MainActivity.kt:15: Error: Methods that can only be accessed in [aa.dd.NewActivity, aa.dd.ee.MainActivity, aa.bb.cc.TheClassShouldBeHide] [HideMember]\n" +
                "            TheClassShouldBeHide().method()\n" +
                "                                   ~~~~~~\n" +
                "src/aa/dd/NewActivity.java:17: Error: Methods that can only be accessed in [aa.dd.NewActivity, aa.dd.ee.MainActivity, aa.bb.cc.TheClassShouldBeHide] [HideMember]\n" +
                "                field.method();\n" +
                "                      ~~~~~~\n" +
                "2 errors, 0 warnings")
    }

    @Test
    fun `Check @Hide method in multi friend class`() {
        TestLintTask.lint().files(
            hideAnnotation,
            kotlin("""
                package aa
                import com.yy.mobile.whisper.Hide

                class TheClassShouldBeHide {

                    @Hide(friend = ["aa.NewActivity", "MainActivity", "aa.bb.cc.NewActivity"])
                    fun method(): Int = 3 + 4
                }
            """.trimIndent()),
            java("""
                package aa.dd;
                import aa.*;

                public class NewActivity {

                    private TheClassShouldBeHide field = new TheClassShouldBeHide();

                    public void method3() {
                        field.method();
                    }
                }
            """.trimIndent()),
            kotlin("""
                package aa.dd.ee.cc.dd
                import aa.TheClassShouldBeHide;

                class MainActivity : android.support.v7.app.AppCompatActivity() {

                    override fun onCreate() {
                        TheClassShouldBeHide().method2()
                        TheClassShouldBeHide().method()
                    }

                    inner class NewActivity {

                        init {
                            TheClassShouldBeHide().method2()
                            TheClassShouldBeHide().method()
                        }
                    }
                }
            """.trimIndent()))
            .detector(WhisperHideDetector())
            .run()
            .expect("src/aa/dd/NewActivity.java:9: Error: Methods that can only be accessed in [aa.NewActivity, MainActivity, aa.bb.cc.NewActivity, aa.TheClassShouldBeHide] [HideMember]\n" +
                "        field.method();\n" +
                "              ~~~~~~\n" +
                "1 errors, 0 warnings")
    }

    @Test
    fun `Check @Hide constructor in unfriendly class`() {
        TestLintTask.lint().files(
            hideAnnotation,
            java("""
                package aa.bb.cc;
                import com.yy.mobile.whisper.Hide;

                public class TheClassShouldBeHide {

                    @Hide(friend = {"NewActivity"})
                    TheClassShouldBeHide() {
                    }
                }
            """.trimIndent()),
            java("""
                package aa.bb.cc;

                public class NewActivity {

                    public void method(){
                        TheClassShouldBeHide notHide = new TheClassShouldBeHide();
                    }
                }
            """.trimIndent()),
            java("""
                package aa.bb.cc;

                public class NewActivit {

                    public void method(){
                        TheClassShouldBeHide shouldHide = new TheClassShouldBeHide();
                    }
                }
            """.trimIndent()),
            kotlin("""
                package aa.bb.cc

                class OldActivity {

                    fun method(){
                        val shouldHide = TheClassShouldBeHide();
                    }
                }
            """.trimIndent()))
            .detector(WhisperHideDetector())
            .run()
            .expect("src/aa/bb/cc/NewActivit.java:6: Error: Methods that can only be accessed in [NewActivity, aa.bb.cc.TheClassShouldBeHide] [HideMember]\n" +
                "        TheClassShouldBeHide shouldHide = new TheClassShouldBeHide();\n" +
                "                                          ~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "src/aa/bb/cc/OldActivity.kt:6: Error: Methods that can only be accessed in [NewActivity, aa.bb.cc.TheClassShouldBeHide] [HideMember]\n" +
                "        val shouldHide = TheClassShouldBeHide();\n" +
                "                         ~~~~~~~~~~~~~~~~~~~~\n" +
                "2 errors, 0 warnings")
    }

    @Test
    fun `Check @Hide Kotlin setter`() {
        TestLintTask.lint().files(
            hideAnnotation,
            kotlin("""
                package aa.bb
                
                import com.yy.mobile.whisper.Hide
                
                class TheClassShouldBeHide {
                    var a: Int = 3
                        @Hide(friend = ["Friend"])
                        set(value) {
                            field = value
                        }
                }
            """.trimIndent()),
            java("""
                package aa.bb;

                public class NotFriend {

                    NotFriend() {
                        TheClassShouldBeHide instance = new TheClassShouldBeHide();
                        instance.setA(3);
                    }
                }
            """.trimIndent()),
            java("""
                package aa.bb;

                public class Friend {

                    Friend() {
                        TheClassShouldBeHide instance = new TheClassShouldBeHide();
                        instance.setA(3);
                    }
                }
            """.trimIndent()),
            kotlin("""
                package aa.cc
                import aa.bb.TheClassShouldBeHide
                
                public class Friend {

                    init {
                        val instance = TheClassShouldBeHide()
                        instance.a = 3;
                    }
                }
            """.trimIndent()),
            kotlin("""
                package aa.cc
                import aa.bb.TheClassShouldBeHide
                
                public class NotFriend {

                    init {
                        val instance = TheClassShouldBeHide()
                        instance.a = 3;
                    }
                }
            """.trimIndent()))
            .detector(WhisperHideDetector())
            .run()
            .expect("" +
                "src/aa/bb/NotFriend.java:7: Error: Methods that can only be accessed in [Friend, aa.bb.TheClassShouldBeHide] [HideMember]\n" +
                "        instance.setA(3);\n" +
                "                 ~~~~\n" +
                "1 errors, 0 warnings\n" +
                "src/aa/cc/NotFriend.kt:8: Error: Methods that can only be accessed in [Friend, aa.bb.TheClassShouldBeHide] [HideMember]\n" +
                "        instance.a;\n" +
                "        ~~~~~~~~~~\n" +
                "2 errors, 0 warnings")
    }

    @Test
    fun `Check @Hide Kotlin getter`() {
        TestLintTask.lint().files(
            hideAnnotation,
            kotlin("""
                package aa.bb
                
                import com.yy.mobile.whisper.Hide
                
                class TheClassShouldBeHide {
                    var a: Int = 3
                        @Hide(friend = ["Friend"])
                        get() = field
                }
            """.trimIndent()),
            java("""
                package aa.bb;

                public class NotFriend {

                    NotFriend() {
                        TheClassShouldBeHide instance = new TheClassShouldBeHide();
                        instance.getA();
                    }
                }
            """.trimIndent()),
            java("""
                package aa.bb;

                public class Friend {

                    Friend() {
                        TheClassShouldBeHide instance = new TheClassShouldBeHide();
                        instance.getA();
                    }
                }
            """.trimIndent()),
            kotlin("""
                package aa.cc
                import aa.bb.TheClassShouldBeHide
                
                public class Friend {

                    init {
                        val instance = TheClassShouldBeHide()
                        instance.a;
                    }
                }
            """.trimIndent()),
            kotlin("""
                package aa.cc
                import aa.bb.TheClassShouldBeHide
                
                public class NotFriend {

                    init {
                        val instance = TheClassShouldBeHide()
                        instance.a;
                    }
                }
            """.trimIndent()))
            .detector(WhisperHideDetector())
            .run()
            .expect("src/aa/bb/NotFriend.java:7: Error: Methods that can only be accessed in [Friend, aa.bb.TheClassShouldBeHide] [HideMember]\n" +
                "        instance.getA();\n" +
                "                 ~~~~\n" +
                "src/aa/cc/NotFriend.kt:8: Error: Methods that can only be accessed in [Friend, aa.bb.TheClassShouldBeHide] [HideMember]\n" +
                "        instance.a;\n" +
                "        ~~~~~~~~~~\n" +
                "2 errors, 0 warnings")
    }
}