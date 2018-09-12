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

    @Test
    fun `Check Java field @Immutable set and modify`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class A {

                    @Immutable
                    private Set<String> set = new LinkedHashSet<String>() {
                        {
                            add("1");
                        }
                    };

                    public void a() {
                        for (Iterator it = set.iterator(); it.hasNext(); ) {
                            if (it.next().equals("1")) {
                                it.remove(); //should lint
                            }
                        }
                    }

                    public void b() {
                        set.clear(); //should lint

                        if (set.containsAll(Collections.singleton("1"))) {
                            if (set.size() > 3) {
                                set = new LinkedHashSet<>(set);
                            }
                        }

                        for (Object a : set.toArray()) {
                            a.getClass();
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
                    private List<String> list = Arrays.asList("1", "2");

                    @Immutable
                    private LinkedHashSet<String> set = new LinkedHashSet<>(list);

                    public void a() {
                        for (String a : list) {
                            a = a.replace('1', '2');
                        }
                        List<String> newList = list.subList(0, 2);
                        set = new LinkedHashSet<>(newList);
                        set.remove("3"); //should lint
                    }

                    public void b() {
                        if (!set.isEmpty()) {
                            set.remove("1"); //should lint
                        }
                        TreeSet<String> newSet = new TreeSet<>(set);
                        newSet.remove("1"); //should not lint
                    }
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("src/aa/A.java:19: Error: you cannot invoke the [remove()] method on an immutable object. [ImmutableObject]\n" +
                "                it.remove(); //should lint\n" +
                "                ~~~~~~~~~~~\n" +
                "    src/aa/A.java:10: This reference is annotated by @Immutable\n" +
                "src/aa/A.java:25: Error: you cannot invoke the [clear()] method on an immutable object. [ImmutableObject]\n" +
                "        set.clear(); //should lint\n" +
                "        ~~~~~~~~~~~\n" +
                "    src/aa/A.java:10: This reference is annotated by @Immutable\n" +
                "src/aa/B.java:21: Error: you cannot invoke the [remove(\"3\")] method on an immutable object. [ImmutableObject]\n" +
                "        set.remove(\"3\"); //should lint\n" +
                "        ~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:13: This reference is annotated by @Immutable\n" +
                "src/aa/B.java:26: Error: you cannot invoke the [remove(\"1\")] method on an immutable object. [ImmutableObject]\n" +
                "            set.remove(\"1\"); //should lint\n" +
                "            ~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:13: This reference is annotated by @Immutable\n" +
                "4 errors, 0 warnings")
    }

    @Test
    fun `Check Java field @Immutable queue and modify`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;
                import java.util.concurrent.ArrayBlockingQueue;

                public class A {

                    @Immutable
                    private Queue<Long> que = new PriorityQueue<>();

                    public void a() {
                        que.offer(3L); //should lint

                        long a = que.peek(); //should not lint

                        long b = que.poll(); //should lint
                    }

                    public void b() {
                        ArrayDeque<Long> deque = new ArrayDeque<>(que);
                        deque.addFirst(3L); // should not lint

                        que = new ArrayBlockingQueue<>(3);
                        que.remove(); //should lint

                        Iterator<Long> it = que.iterator();
                        do {
                            if (it.next() == 3L) {
                                it.remove(); //should lint
                            }
                        } while (it.hasNext());
                    }
                }
            """.trimIndent()),
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;
                import java.util.concurrent.LinkedBlockingDeque;

                public class B {
                    @Immutable
                    private LinkedBlockingDeque<Long> que = new LinkedBlockingDeque<>();

                    public void a() {
                        que.drainTo(new ArrayList<Long>()); //should lint

                        try {
                            que.putLast(3L); //should lint
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    public void b() {
                        Iterator<Long> it = que.descendingIterator();
                        for (; it.hasNext(); ) {
                            if (it.next() > 3L) {
                                it.remove(); //should lint
                            }
                        }
                    }
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("src/aa/A.java:14: Error: you cannot invoke the [offer(3)] method on an immutable object. [ImmutableObject]\n" +
                "        que.offer(3L); //should lint\n" +
                "        ~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:11: This reference is annotated by @Immutable\n" +
                "src/aa/A.java:18: Error: you cannot invoke the [poll()] method on an immutable object. [ImmutableObject]\n" +
                "        long b = que.poll(); //should lint\n" +
                "                 ~~~~~~~~~~\n" +
                "    src/aa/A.java:11: This reference is annotated by @Immutable\n" +
                "src/aa/A.java:26: Error: you cannot invoke the [remove()] method on an immutable object. [ImmutableObject]\n" +
                "        que.remove(); //should lint\n" +
                "        ~~~~~~~~~~~~\n" +
                "    src/aa/A.java:11: This reference is annotated by @Immutable\n" +
                "src/aa/A.java:31: Error: you cannot invoke the [remove()] method on an immutable object. [ImmutableObject]\n" +
                "                it.remove(); //should lint\n" +
                "                ~~~~~~~~~~~\n" +
                "    src/aa/A.java:11: This reference is annotated by @Immutable\n" +
                "src/aa/B.java:13: Error: you cannot invoke the [drainTo(ArrayList())] method on an immutable object. [ImmutableObject]\n" +
                "        que.drainTo(new ArrayList<Long>()); //should lint\n" +
                "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:10: This reference is annotated by @Immutable\n" +
                "src/aa/B.java:16: Error: you cannot invoke the [putLast(3)] method on an immutable object. [ImmutableObject]\n" +
                "            que.putLast(3L); //should lint\n" +
                "            ~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:10: This reference is annotated by @Immutable\n" +
                "src/aa/B.java:26: Error: you cannot invoke the [remove()] method on an immutable object. [ImmutableObject]\n" +
                "                it.remove(); //should lint\n" +
                "                ~~~~~~~~~~~\n" +
                "    src/aa/B.java:10: This reference is annotated by @Immutable\n" +
                "7 errors, 0 warnings")
    }

    @Test
    fun `Check Java local @Immutable to mutable field`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class A {
                    private List<String> list; //should lint

                    public void a() {

                        @Immutable
                        List<String> localList = new ArrayList<>(Collections.singleton("haha"));

                        localList.add("haha2"); //should lint

                        list = localList;
                        list.add("haha3"); //no necessary lint
                    }
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("src/aa/A.java:8: Error: Unable to assign an immutable object [localList] to a mutable field [list]. [ImmutableEscape]\n" +
                "    private List<String> list; //should lint\n" +
                "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:17: This expression [localList] is immutable.\n" +
                "src/aa/A.java:15: Error: you cannot invoke the [add(\"haha2\")] method on an immutable object. [ImmutableObject]\n" +
                "        localList.add(\"haha2\"); //should lint\n" +
                "        ~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:13: This reference is annotated by @Immutable\n" +
                "2 errors, 0 warnings")
            .expectFixDiffs("Fix for src/aa/A.java line 8: Annotate [list] with @Immutable:\n" +
                "@@ -8 +8\n" +
                "-     private List<String> list; //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable private List<String> list; //should lint")
    }

    @Test
    fun `Check Java local @Immutable to mutable field and fix`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class A {

                    private Set<String> set; //should lint

                    public void a() {

                        @Immutable
                        HashSet<String> localSet = new HashSet<>();

                        HashSet<String> localSet2 = localSet;

                        set = localSet;
                    }
                }
            """.trimIndent()),
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class B {

                    private Set<String> newSet; //should lint

                    @Immutable
                    private Set<String> set;

                    public void a() {

                        set = new HashSet<>();
                        set.clear();

                        newSet = set;
                    }

                    public void b() {

                        newSet.clear();
                    }
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("" +
                "src/aa/A.java:9: Error: Unable to assign an immutable object [localSet] to a mutable field [set]. [ImmutableEscape]\n" +
                "    private Set<String> set; //should lint\n" +
                "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:18: This expression [localSet] is immutable.\n" +
                "src/aa/B.java:9: Error: Unable to assign an immutable object [set] to a mutable field [newSet]. [ImmutableEscape]\n" +
                "    private Set<String> newSet; //should lint\n" +
                "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:19: This expression [set] is immutable.\n" +
                "src/aa/B.java:17: Error: you cannot invoke the [clear()] method on an immutable object. [ImmutableObject]\n" +
                "        set.clear();\n" +
                "        ~~~~~~~~~~~\n" +
                "    src/aa/B.java:11: This reference is annotated by @Immutable\n" +
                "3 errors, 0 warnings")
            .expectFixDiffs("Fix for src/aa/A.java line 9: Annotate [set] with @Immutable:\n" +
                "@@ -9 +9\n" +
                "-     private Set<String> set; //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable private Set<String> set; //should lint\n" +
                "Fix for src/aa/B.java line 9: Annotate [newSet] with @Immutable:\n" +
                "@@ -9 +9\n" +
                "-     private Set<String> newSet; //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable private Set<String> newSet; //should lint")
    }
}