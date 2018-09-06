package com.yy.mobile.whisperlint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask
import org.junit.Test

/**
 * Created by 张宇 on 2018/9/5.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
class WhisperUseWithTest {

    private val useWithAnnotation: TestFile = java("""
                package com.yy.mobile.whisper;

                import java.lang.annotation.Documented;
                import java.lang.annotation.ElementType;
                import java.lang.annotation.Retention;
                import java.lang.annotation.RetentionPolicy;
                import java.lang.annotation.Target;

                @Documented
                @Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
                @Retention(RetentionPolicy.SOURCE)
                public @interface UseWith {

                    String value();
                }
    """.trimIndent())

    @Test
    fun `Check simple Java init and unInit`() {

        TestLintTask.lint().files(
            useWithAnnotation,
            java("""
                package cc;
                import com.yy.mobile.whisper.UseWith;

                public class A {

                    @UseWith("unInit")
                    public void init() {
                        int a = 3 + 4;
                    }

                    public void unInit() {
                        int b = 4 - 3;
                    }
                }
            """.trimIndent()),
            java("""
                package cc;

                public class B {

                    private A a = new A();

                    void onCreate() {
                        a.init();
                    }

                    void onDestroy() {
                        a.unInit();
                    }
                }
            """.trimIndent()),
            kotlin("""
                package ccc;
                import cc.*;

                public class C {

                    val a = A()

                    init {
                        a.init()
                    }

                    private fun onDestroy() {
                        a.unInit()
                    }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun `Check simple Kotlin init without unInit`(){
        TestLintTask.lint().files(
            useWithAnnotation,
            kotlin("""
                package cc

                import com.yy.mobile.whisper.*

                class A {

                    @UseWith("deInit")
                    fun init() {
                        val a = 3 + 4;
                    }

                    fun deInit() {
                        val b = 4 - 3;
                    }
                }
            """.trimIndent()),
            java("""
                package cc;

                public class B {

                    private A a = new A();

                    void onCreate() {
                        a.init();
                    }

                    private void deInit(){

                    }
                }
            """.trimIndent()),
            kotlin("""
                package ccc
                import cc.A

                class C {

                    val a = A()

                    init{
                        a.init()

                        deInit()
                    }

                    fun deInit(){

                    }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("src/cc/B.java:8: Warning: a.init() must be used with deInit [MissingUsage]\n" +
                "        a.init();\n" +
                "        ~~~~~~~~\n" +
                "src/ccc/C.kt:9: Warning: init() must be used with deInit [MissingUsage]\n" +
                "        a.init()\n" +
                "        ~~~~~~~~\n" +
                "0 errors, 2 warnings")
    }

    @Test
    fun `Check simple Java init without unInit`(){
        TestLintTask.lint().files(
            useWithAnnotation,
            java("""
                package cc;

                import com.yy.mobile.whisper.*;

                public class A {

                    @UseWith("deInit")
                    public void init() {
                        int a = 3 + 4;
                    }

                    public void deInit() {
                        int b = 4 - 3;
                    }
                }
            """.trimIndent()),
            java("""
                package cc;

                public class B {

                    private A a = new A();

                    void onCreate() {
                        a.init();
                    }

                    private void deInit(){

                    }
                }
            """.trimIndent()),
            kotlin("""
                package ccc
                import cc.A

                class C {

                    val a = A()

                    init{
                        a.init()

                        deInit()
                    }

                    fun deInit(){

                    }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("src/cc/B.java:8: Warning: a.init() must be used with deInit [MissingUsage]\n" +
                "        a.init();\n" +
                "        ~~~~~~~~\n" +
                "src/ccc/C.kt:9: Warning: init() must be used with deInit [MissingUsage]\n" +
                "        a.init()\n" +
                "        ~~~~~~~~\n" +
                "0 errors, 2 warnings")
    }

    @Test
    fun `Check Java init without unInit in different scope`(){
        TestLintTask.lint().files(
            useWithAnnotation,
            java("""
                package cc;

                import com.yy.mobile.whisper.*;

                public class A {

                    @UseWith("deInit")
                    public void init() {
                        int a = 3 + 4;
                    }

                    public void deInit() {
                        int b = 4 - 3;
                    }
                }
            """.trimIndent()),
            java("""
                package cc;

                public class B {

                    private A a = new A();

                    void onCreate() {
                        a.init();
                        A aa = new A();
                        aa.init();
                    }

                    void onDestroy() {
                        a.deInit();

                        new A().deInit();
                    }
                }
            """.trimIndent()),
            kotlin("""
                package ccc
                import cc.A

                class C {

                    val a = A()

                    init{
                        a.let {
                            it.init()
                        }

                        unInit()
                    }

                    fun unInit() {
                        with(A()){
                            deInit()
                        }
                        A().let { it.deInit() }

                        a.apply{
                            deInit()
                        }
                    }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("src/cc/B.java:10: Warning: aa.init() must be used with deInit [MissingUsage]\n" +
                "        aa.init();\n" +
                "        ~~~~~~~~~\n" +
                "0 errors, 1 warnings")
    }

    @Test
    fun `Check Kotlin init without unInit in different scope`(){
        TestLintTask.lint().files(
            useWithAnnotation,
            kotlin("""
                package cc

                import com.yy.mobile.whisper.UseWith

                public class A {

                    @UseWith("a" + "Init")
                    fun init() {
                    }

                    internal fun aInit() {
                    }
                }
            """.trimIndent()),
            java("""
                package cc;

                public class B {

                    private A a = new A();

                    void onCreate() {
                        a.init();
                        final A aa = new A();
                        aa.init();

                        onCallback(new Callback() {
                            @Override
                            public void haha() {
                                aa.aInit();
                            }
                        });
                    }

                    void onCallback(Callback cb) {
                        if (cb != null) {
                            A b = a;
                            b.aInit();
                        }
                    }

                    interface Callback {
                        void haha();
                    }
                }
            """.trimIndent()),
            kotlin("""
                package ccc
                import cc.A

                class C {

                    val a = A()

                    init{
                        a.let {
                            it.init()
                        }

                        unInit()
                    }

                    fun unInit() {
                        with(A()){
                            a.deInit()
                        }

                        val b = A().also { it.init() }

                        b.apply{
                            init()
                        }
                    }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("src/cc/B.java:10: Warning: aa.init() must be used with deInit [MissingUsage]\n" +
                "        aa.init();\n" +
                "        ~~~~~~~~~\n" +
                "0 errors, 1 warnings")
    }
}