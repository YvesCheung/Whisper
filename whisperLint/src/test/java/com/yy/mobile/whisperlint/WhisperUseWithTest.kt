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
    fun `Check simple Kotlin init without unInit`() {
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
    fun `Check simple Java init without unInit`() {
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
    fun `Check Java init without unInit in different scope`() {
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
    fun `Check Kotlin init and unInit in different scope`() {
        TestLintTask.lint().files(
            useWithAnnotation,
            kotlin("""
                package cc

                import com.yy.mobile.whisper.UseWith

                public class A {

                    @UseWith("a" + "Init")
                    fun init() {
                    }

                    fun aInit() {
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
                            a.aInit()
                        }

                        val b = A().also { it.init() }

                        b.apply{
                            aInit()
                        }
                    }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun `Check Kotlin init without unInit in different scope`() {
        TestLintTask.lint().files(
            useWithAnnotation,
            kotlin("""
                package cc

                import com.yy.mobile.whisper.UseWith

                public class A {

                    @UseWith("a" + "Init")
                    fun init() {
                    }

                    fun aInit() {
                    }
                }
            """.trimIndent()),
            java("""
                package cc;

                public class B {

                    private A a = new A();

                    void onCreate() {
                        a.init(); //should not lint
                        final A aa = new A();
                        aa.init(); // should lint

                        onCallback(new Callback() {
                            @Override
                            public void haha() {
                                a.aInit();
                            }
                        });
                    }

                    void onCallback(Callback cb) {
                        A aa = new A();
                        aa.aInit();
                    }

                    interface Callback {
                        void haha();
                    }
                }
            """.trimIndent()),
            kotlin("""
                package dd
                import cc.A

                class C {

                    val a = A()

                    init {
                        a.init() // should lint

                        a.let {
                            aInit() // without it
                        }
                    }

                    fun aInit() {
                        var b: A = A().also {
                            it.init() // should lint
                        }

                        b = A().also {
                            it.init() // should not lint
                        }

                        b.aInit()
                    }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("src/cc/B.java:10: Warning: aa.init() must be used with aInit [MissingUsage]\n" +
                "        aa.init(); // should lint\n" +
                "        ~~~~~~~~~\n" +
                "src/dd/C.kt:9: Warning: init() must be used with aInit [MissingUsage]\n" +
                "        a.init() // should lint\n" +
                "        ~~~~~~~~\n" +
                "src/dd/C.kt:18: Warning: init() must be used with aInit [MissingUsage]\n" +
                "            it.init() // should lint\n" +
                "            ~~~~~~~~~\n" +
                "0 errors, 3 warnings")
    }

    @Test
    fun `Check Kotlin init and unInit in Kotlin extend fuction`() {
        TestLintTask.lint().files(
            useWithAnnotation,
            kotlin("""
                package cc

                import com.yy.mobile.whisper.UseWith

                public class A {

                    @UseWith("aInit")
                    fun init() {
                    }

                    fun aInit() {
                    }
                }
            """.trimIndent()),
            kotlin("""
                package dd
                import cc.A

                class B {

                    val a = A().apply { init() }

                    fun aInit() {
                        a.let { it.aInit() }
                    }
                }
            """.trimIndent()),
            kotlin("""
                package dd
                import cc.A

                class C {

                    val a = A().apply { init() }

                    fun aInit() {
                        a.apply { aInit() }
                    }
                }
            """.trimIndent()),
            kotlin("""
                package dd
                import cc.A

                class D {

                    val a = A().apply { init() }

                    fun aInit() {
                        a.also {
                            it.aInit()
                        }
                    }
                }
            """.trimIndent()),
            kotlin("""
                package dd
                import cc.A

                class E {

                    val a = A().apply { init() }

                    fun aInit() {
                        a.run { aInit() }
                    }
                }
            """.trimIndent()),
            kotlin("""
                package dd
                import cc.A

                class F {

                    val a = A().apply { init() }

                    fun aInit() {
                        a.aInit()
                    }
                }
            """.trimIndent()),
            kotlin("""
                package dd
                import cc.A

                class G {

                    val a = A().apply { init() }

                    fun aInit() {
                        with(a){
                            aInit()
                        }
                    }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun `Check Kotlin init and unInit in Kotlin assignment`() {
        TestLintTask.lint().files(
            useWithAnnotation,
            kotlin("""
                package cc

                import com.yy.mobile.whisper.UseWith

                public class A {

                    @UseWith("deInit")
                    fun init() {
                    }

                    fun deInit() {
                    }
                }
            """.trimIndent()),
            kotlin("""
                package dd
                import cc.A

                class B {

                    val a = A().apply { init() }

                    fun aInit() {
                        val b = a
                        b.let { it.deInit() }
                    }
                }
            """.trimIndent()),
            kotlin("""
                package dd
                import cc.A

                class C {

                    val a: A by lazy {
                        A().also { it.init() }
                    }

                    fun haha() {
                        val b = a
                        val c = b
                        c.apply { deInit() }
                    }
                }
            """.trimIndent()),
            kotlin("""
                package dd
                import cc.A

                class D {

                    val a = A().apply { init() }

                    fun haha() {
                        val b = a
                        val c = b
                        c.apply { deInit() }
                    }
                }
            """.trimIndent()),
            kotlin("""
                package dd
                import cc.A

                class E {

                    var a: A? = null

                    fun haha() {
                        a = A().apply { init() }

                        val c = a
                        c?.apply { deInit() }
                    }
                }
            """.trimIndent()),
            kotlin("""
                package dd
                import cc.A

                class F {

                    var a: A? = null

                    fun haha() {
                        val b = A().apply { init() }

                        a = b

                        a?.let { it.deInit() }
                    }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun `Check Kotlin init and unInit in Kotlin special function`() {
        TestLintTask.lint().files(
            useWithAnnotation,
            kotlin("""
                package cc

                import com.yy.mobile.whisper.UseWith

                class A {

                    @UseWith("deInit")
                    fun init() {
                    }

                    fun deInit() {
                    }
                }
            """.trimIndent()),
            kotlin("""
                package dd
                import cc.A

                class B {

                    val a = A()

                    fun aInit() {

                        with(a){
                            init()
                        }

                        val b = a
                        b.let { it.deInit() }
                    }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun `Check Kotlin init and unInit in Kotlin extend fuction and assignment`() {
        TestLintTask.lint().files(
            useWithAnnotation,
            kotlin("""
                package cc

                import com.yy.mobile.whisper.UseWith

                public class A {

                    @UseWith("aInit")
                    fun init() = 4

                    fun aInit(): Int = 3
                }
            """.trimIndent()),
            kotlin("""
                package dd
                import cc.A

                class B {

                    val a = A().apply {
                        val unused = init()
                    }

                    fun aInit() {
                        a.let {
                            val unused = it.aInit()
                        }
                    }
                }
            """.trimIndent()),
            kotlin("""
                package dd
                import cc.A

                class C {

                    val a = A().apply {
                        val unused = init()
                    }

                    fun aInit() {
                        a.apply {
                            val unused = aInit()
                        }
                    }
                }
            """.trimIndent()),
            kotlin("""
                package dd
                import cc.A

                class D {

                    val a by lazy {
                        A().apply { val unused = init() }
                    }

                    fun aInit() {
                        a.also {
                            val unused = it.aInit()
                        }
                    }
                }
            """.trimIndent()),
            kotlin("""
                package dd
                import cc.A

                class E {

                    val a = A()

                    fun aInit() {
                        val unused1 = a.init()

                        a.run { val unused2 = aInit() }
                    }
                }
            """.trimIndent()),
            kotlin("""
                package dd
                import cc.A

                class F {

                    val a = A()

                    fun aInit() {

                        val unused1 = a.let { h -> h.init() }
                        val unused2 = a.aInit()
                    }
                }
            """.trimIndent()),
            kotlin("""
                package dd
                import cc.A

                class G {

                    val a = A()

                    fun aInit() {
                        with(A()){
                            val unused1 = init()
                            val unused2 = aInit()
                        }

                        with(a) {
                            val unused3 = init()
                            val unused4 = aInit()
                        }

                        val b = A()
                        with(b) {
                            val unused5 = init()
                            val unused6 = aInit()
                        }
                    }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("No warnings.")
    }

    @Test
    fun `Check Kotlin init and unInit on different object`() {
        TestLintTask.lint().files(
            useWithAnnotation,
            kotlin("""
                package cc

                import com.yy.mobile.whisper.UseWith

                class A {

                    @UseWith("deInit")
                    fun init() {
                    }

                    fun deInit() {
                    }
                }
            """.trimIndent()),
            kotlin("""
                package dd
                import cc.A

                class B {

                    val global = A()

                    fun a() {

                        with(A()) {
                            init() //should not lint
                            deInit()
                        }

                        A().let{
                            it.init() //should not lint
                            it.deInit()
                        }
                    }

                    fun b() {
                        val local = A()
                        local.init() //should not lint
                        local.deInit()

                        global.init() //should not lint
                        global.deInit()

                        A().init()   //should lint
                        A().deInit()
                    }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("src/dd/B.kt:29: Warning: init() must be used with deInit [MissingUsage]\n" +
                "        A().init()   //should lint\n" +
                "        ~~~~~~~~~~\n" +
                "0 errors, 1 warnings")
    }

    @Test
    fun `Check Java init and unInit on different object`() {
        TestLintTask.lint().files(
            useWithAnnotation,
            java("""
                package cc;

                import com.yy.mobile.whisper.UseWith;

                public class A {

                    @UseWith("deInit")
                    void init() {
                    }

                    void deInit() {
                    }
                }
            """.trimIndent()),
            kotlin("""
                package dd
                import cc.A

                class B {

                    val global = A()

                    fun a() {

                        with(A()) {
                            init() //should not lint
                            deInit()
                        }

                        A().let{
                            it.init() //should not lint
                            it.deInit()
                        }
                    }

                    fun b() {
                        val local = A()
                        local.init() //should not lint
                        local.deInit()

                        global.init() //should not lint
                        global.deInit()

                        A().init()   //should lint
                        A().deInit()
                    }
                }
            """.trimIndent()),
            java("""
                package cc;

                public class C {

                    void a() {
                        new A().init();
                        new A().deInit(); //should lint
                    }

                    private A instance;

                    void b() {
                        instance = new A();

                        instance.init(); //should not lint
                    }

                    void c() {
                        instance.deInit();
                    }
                }
            """.trimIndent()))
            .detector(WhisperUseWithDetector())
            .run()
            .expect("")
    }
}