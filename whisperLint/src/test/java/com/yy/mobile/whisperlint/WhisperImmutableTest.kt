package com.yy.mobile.whisperlint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestLintTask
import org.junit.Test

/**
 * Created by 张宇 on 2018/9/10.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
class WhisperImmutableTest {

    private val immutableAnnotation: TestFile = java("""
                package com.yy.mobile.whisper;

                import java.lang.annotation.Documented;
                import java.lang.annotation.ElementType;
                import java.lang.annotation.Retention;
                import java.lang.annotation.RetentionPolicy;
                import java.lang.annotation.Target;

                @Documented
                @Target({ElementType.FIELD,
                        ElementType.METHOD,
                        ElementType.PARAMETER})
                @Retention(RetentionPolicy.SOURCE)
                public @interface Immutable {
                }
    """.trimIndent())

    @Test
    fun `Check Java field @Immutable collection and modify`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.ArrayList;
                import java.util.Collections;
                import java.util.List;

                public class A {

                    @Immutable
                    private List<String> list = new ArrayList<>();

                    public void a() {
                        list.add("haha");

                        list.remove("haha");

                        list.addAll(Collections.singleton("haha"));

                        list.removeAll(Collections.singleton("haha"));

                        list.clear();

                        list.retainAll(Collections.singleton("haha"));

                        list.iterator().remove();
                    }
                }
            """.trimIndent()),
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class B {

                    @Immutable
                    private Collection<String> list = new ArrayList<>();

                    public void a() {
                        list.add("haha");

                        list.remove("haha");

                        list.addAll(Collections.singleton("haha"));

                        list.removeAll(Collections.singleton("haha"));

                        list.clear();

                        list.retainAll(Collections.singleton("haha"));

                        list.iterator().remove();
                    }
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("src/aa/A.java:15: Error: you cannot invoke the [add(\"haha\")] method on an immutable object. [ImmutableObject]\n" +
                "        list.add(\"haha\");\n" +
                "        ~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:12: This reference is annotated by @Immutable\n" +
                "src/aa/A.java:17: Error: you cannot invoke the [remove(\"haha\")] method on an immutable object. [ImmutableObject]\n" +
                "        list.remove(\"haha\");\n" +
                "        ~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:12: This reference is annotated by @Immutable\n" +
                "src/aa/A.java:19: Error: you cannot invoke the [addAll(Collections.singleton(\"haha\"))] method on an immutable object. [ImmutableObject]\n" +
                "        list.addAll(Collections.singleton(\"haha\"));\n" +
                "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:12: This reference is annotated by @Immutable\n" +
                "src/aa/A.java:21: Error: you cannot invoke the [removeAll(Collections.singleton(\"haha\"))] method on an immutable object. [ImmutableObject]\n" +
                "        list.removeAll(Collections.singleton(\"haha\"));\n" +
                "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:12: This reference is annotated by @Immutable\n" +
                "src/aa/A.java:23: Error: you cannot invoke the [clear()] method on an immutable object. [ImmutableObject]\n" +
                "        list.clear();\n" +
                "        ~~~~~~~~~~~~\n" +
                "    src/aa/A.java:12: This reference is annotated by @Immutable\n" +
                "src/aa/A.java:25: Error: you cannot invoke the [retainAll(Collections.singleton(\"haha\"))] method on an immutable object. [ImmutableObject]\n" +
                "        list.retainAll(Collections.singleton(\"haha\"));\n" +
                "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:12: This reference is annotated by @Immutable\n" +
                "src/aa/A.java:27: Error: you cannot invoke the [remove()] method on an immutable object. [ImmutableObject]\n" +
                "        list.iterator().remove();\n" +
                "        ~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:12: This reference is annotated by @Immutable\n" +
                "src/aa/B.java:13: Error: you cannot invoke the [add(\"haha\")] method on an immutable object. [ImmutableObject]\n" +
                "        list.add(\"haha\");\n" +
                "        ~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:10: This reference is annotated by @Immutable\n" +
                "src/aa/B.java:15: Error: you cannot invoke the [remove(\"haha\")] method on an immutable object. [ImmutableObject]\n" +
                "        list.remove(\"haha\");\n" +
                "        ~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:10: This reference is annotated by @Immutable\n" +
                "src/aa/B.java:17: Error: you cannot invoke the [addAll(Collections.singleton(\"haha\"))] method on an immutable object. [ImmutableObject]\n" +
                "        list.addAll(Collections.singleton(\"haha\"));\n" +
                "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:10: This reference is annotated by @Immutable\n" +
                "src/aa/B.java:19: Error: you cannot invoke the [removeAll(Collections.singleton(\"haha\"))] method on an immutable object. [ImmutableObject]\n" +
                "        list.removeAll(Collections.singleton(\"haha\"));\n" +
                "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:10: This reference is annotated by @Immutable\n" +
                "src/aa/B.java:21: Error: you cannot invoke the [clear()] method on an immutable object. [ImmutableObject]\n" +
                "        list.clear();\n" +
                "        ~~~~~~~~~~~~\n" +
                "    src/aa/B.java:10: This reference is annotated by @Immutable\n" +
                "src/aa/B.java:23: Error: you cannot invoke the [retainAll(Collections.singleton(\"haha\"))] method on an immutable object. [ImmutableObject]\n" +
                "        list.retainAll(Collections.singleton(\"haha\"));\n" +
                "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:10: This reference is annotated by @Immutable\n" +
                "src/aa/B.java:25: Error: you cannot invoke the [remove()] method on an immutable object. [ImmutableObject]\n" +
                "        list.iterator().remove();\n" +
                "        ~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:10: This reference is annotated by @Immutable\n" +
                "14 errors, 0 warnings")
    }

    @Test
    fun `Check Java field @Immutable collection and static init and modify`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class A {

                    @Immutable
                    private List<String> list = new ArrayList<>() {
                        {
                            add("haha");
                            remove("haha");
                        }
                    };

                    public void a() {
                        list.add("wtf");
                    }
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("src/aa/A.java:18: Error: you cannot invoke the [add(\"wtf\")] method on an immutable object. [ImmutableObject]\n" +
                "        list.add(\"wtf\");\n" +
                "        ~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:10: This reference is annotated by @Immutable\n" +
                "1 errors, 0 warnings")
    }

    @Test
    fun `Check Java field @Immutable collection iterator and modify`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class A {

                    @Immutable
                    private LinkedList<String> list = new LinkedList<>();

                    public void a() {
                        for (Iterator<String> it = list.iterator(); it.hasNext(); ) {
                            String c = it.next();
                        }
                    }

                    public void b() {
                        Iterator<? extends CharSequence> it = list.iterator();
                        while (it.hasNext()) {
                            if (it.next().equals("haha")) {
                                it.remove();
                            }
                        }
                    }
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("src/aa/A.java:22: Error: you cannot invoke the [remove()] method on an immutable object. [ImmutableObject]\n" +
                "                it.remove();\n" +
                "                ~~~~~~~~~~~\n" +
                "    src/aa/A.java:10: This reference is annotated by @Immutable\n" +
                "1 errors, 0 warnings")
    }

    @Test
    fun `Check Java field @Immutable map and modify`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class A {

                    @Immutable
                    private Map<String, String> map = new LinkedHashMap<>();

                    public void a() {
                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            if (entry.getKey().equals(entry.getValue())) {
                                entry.setValue("asd"); //should lint
                            }
                        }
                    }

                    public void b() {
                        Collection<String> collection = map.values();
                        if (collection.isEmpty()) {
                            collection.add("asd"); //should lint
                        }
                        Iterator<String> it = collection.iterator();
                        while (it.hasNext()) {
                            if (it.next().equals("haha")) {
                                it.remove(); //should lint
                            }
                        }
                    }
                }
            """.trimIndent()),
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class B {

                    @Immutable
                    private TreeMap<Integer, String> map = new TreeMap<>();

                    public void a() {
                        map.putAll(Collections.unmodifiableMap(
                                Collections.<Integer, String>emptyMap())); //should lint
                    }

                    public void b() {
                        map.subMap(1, 10).remove(3); //should lint
                    }

                    public void c() {
                        if (map.containsKey(3)) {
                            for (Integer a : map.keySet()) {
                                String value = map.get(a);
                            }
                        }
                    }
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("src/aa/A.java:15: Error: you cannot invoke the [setValue(\"asd\")] method on an immutable object. [ImmutableObject]\n" +
                "                entry.setValue(\"asd\"); //should lint\n" +
                "                ~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:10: This reference is annotated by @Immutable\n" +
                "src/aa/A.java:23: Error: you cannot invoke the [add(\"asd\")] method on an immutable object. [ImmutableObject]\n" +
                "            collection.add(\"asd\"); //should lint\n" +
                "            ~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:10: This reference is annotated by @Immutable\n" +
                "src/aa/A.java:28: Error: you cannot invoke the [remove()] method on an immutable object. [ImmutableObject]\n" +
                "                it.remove(); //should lint\n" +
                "                ~~~~~~~~~~~\n" +
                "    src/aa/A.java:10: This reference is annotated by @Immutable\n" +
                "src/aa/B.java:13: Error: you cannot invoke the [putAll(Collections.unmodifiableMap(Collections.emptyMap()))] method on an immutable object. [ImmutableObject]\n" +
                "        map.putAll(Collections.unmodifiableMap(\n" +
                "        ^\n" +
                "    src/aa/B.java:10: This reference is annotated by @Immutable\n" +
                "src/aa/B.java:18: Error: you cannot invoke the [remove(3)] method on an immutable object. [ImmutableObject]\n" +
                "        map.subMap(1, 10).remove(3); //should lint\n" +
                "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:10: This reference is annotated by @Immutable\n" +
                "5 errors, 0 warnings")
    }
}