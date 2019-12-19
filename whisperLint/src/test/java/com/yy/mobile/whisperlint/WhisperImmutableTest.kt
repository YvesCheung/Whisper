package com.yy.mobile.whisperlint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestLintTask
import org.junit.Test

/**
 * @author YvesCheung
 * 2018/9/10.
 *
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
                        ElementType.LOCAL_VARIABLE,
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
    fun `Check Java local @Immutable map and modify`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class A {

                    public void a() {

                        @Immutable
                        Map<String, String> map = new LinkedHashMap<>();

                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            if (entry.getKey().equals(entry.getValue())) {
                                entry.setValue("asd"); //should lint
                            }
                        }

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

                    public void a() {

                        @Immutable
                        TreeMap<Integer, String> map = new TreeMap<>();

                        map.putAll(Collections.unmodifiableMap(
                                Collections.<Integer, String>emptyMap())); //should lint

                        map.subMap(1, 10).remove(3); //should lint

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
            .expect("src/aa/A.java:16: Error: you cannot invoke the [setValue(\"asd\")] method on an immutable object. [ImmutableObject]\n" +
                "                entry.setValue(\"asd\"); //should lint\n" +
                "                ~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:12: This reference is annotated by @Immutable\n" +
                "src/aa/A.java:22: Error: you cannot invoke the [add(\"asd\")] method on an immutable object. [ImmutableObject]\n" +
                "            collection.add(\"asd\"); //should lint\n" +
                "            ~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:12: This reference is annotated by @Immutable\n" +
                "src/aa/A.java:27: Error: you cannot invoke the [remove()] method on an immutable object. [ImmutableObject]\n" +
                "                it.remove(); //should lint\n" +
                "                ~~~~~~~~~~~\n" +
                "    src/aa/A.java:12: This reference is annotated by @Immutable\n" +
                "src/aa/B.java:14: Error: you cannot invoke the [putAll(Collections.unmodifiableMap(Collections.emptyMap()))] method on an immutable object. [ImmutableObject]\n" +
                "        map.putAll(Collections.unmodifiableMap(\n" +
                "        ^\n" +
                "    src/aa/B.java:12: This reference is annotated by @Immutable\n" +
                "src/aa/B.java:17: Error: you cannot invoke the [remove(3)] method on an immutable object. [ImmutableObject]\n" +
                "        map.subMap(1, 10).remove(3); //should lint\n" +
                "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:12: This reference is annotated by @Immutable\n" +
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

    @Test
    fun `Check Java method @Immutable to local`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class A {

                    @Immutable
                    public Map<Long, String> a() {
                        return new HashMap<Long, String>();
                    }

                    private void b() {
                        Map<Long, String> local = a();

                        Set<Long> keySet = local.keySet();
                        keySet.clear(); //should lint

                        for (Map.Entry<Long, String> entry : local.entrySet()) {
                            entry.setValue("haha"); //should lint
                        }
                    }
                }
            """.trimIndent()),
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class B {

                    public void a() {
                        Map<Long, String> local = new A().a();

                        local.putAll(Collections.singletonMap(4L, "ll")); //should lint
                    }

                    public void b() {
                        new A().a().put(5L, "aa"); //should lint
                    }
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("src/aa/A.java:18: Error: you cannot invoke the [clear()] method on an immutable object. [ImmutableObject]\n" +
                "        keySet.clear(); //should lint\n" +
                "        ~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:15: This reference is annotated by @Immutable\n" +
                "src/aa/A.java:21: Error: you cannot invoke the [setValue(\"haha\")] method on an immutable object. [ImmutableObject]\n" +
                "            entry.setValue(\"haha\"); //should lint\n" +
                "            ~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:15: This reference is annotated by @Immutable\n" +
                "src/aa/B.java:12: Error: you cannot invoke the [putAll(Collections.singletonMap(4, \"ll\"))] method on an immutable object. [ImmutableObject]\n" +
                "        local.putAll(Collections.singletonMap(4L, \"ll\")); //should lint\n" +
                "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:10: This reference is annotated by @Immutable\n" +
                "src/aa/B.java:16: Error: you cannot invoke the [put(5, \"aa\")] method on an immutable object. [ImmutableObject]\n" +
                "        new A().a().put(5L, \"aa\"); //should lint\n" +
                "        ~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:16: This reference is annotated by @Immutable\n" +
                "4 errors, 0 warnings")
    }

    @Test
    fun `Check Java method @Immutable to field`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class A {

                    private Map<Long, String> field; //should lint

                    @Immutable
                    public Map<Long, String> a() {
                        Map<Long, String> map = new HashMap<>();
                        map.put(3L, "ahah");
                        return map;
                    }

                    private void b() {
                        field = a();

                        Set<Long> keySet = field.keySet();
                        keySet.clear(); //should lint

                        for (Map.Entry<Long, String> entry : field.entrySet()) {
                            entry.setValue("haha"); //should lint
                        }
                    }
                }
            """.trimIndent()),
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class B {

                    public A getA() {
                        return new A();
                    }

                    Map<Long, String> field = new B().getA().a(); //should lint

                    public void a() {
                        field.putAll(Collections.singletonMap(4L, "ll")); //should lint
                    }

                    public void b() {
                        field.put(5L, "aa"); //should lint
                    }
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("src/aa/A.java:9: Error: Unable to assign an immutable object [a()] to a mutable field [field]. [ImmutableEscape]\n" +
                "    private Map<Long, String> field; //should lint\n" +
                "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:19: This expression [a()] is immutable.\n" +
                "src/aa/B.java:13: Error: Unable to assign an immutable object [new B().getA().a()] to a mutable field [field]. [ImmutableEscape]\n" +
                "    Map<Long, String> field = new B().getA().a(); //should lint\n" +
                "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:13: This expression [new B().getA().a()] is immutable.\n" +
                "src/aa/A.java:22: Error: you cannot invoke the [clear()] method on an immutable object. [ImmutableObject]\n" +
                "        keySet.clear(); //should lint\n" +
                "        ~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:19: This reference is annotated by @Immutable\n" +
                "src/aa/A.java:25: Error: you cannot invoke the [setValue(\"haha\")] method on an immutable object. [ImmutableObject]\n" +
                "            entry.setValue(\"haha\"); //should lint\n" +
                "            ~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:19: This reference is annotated by @Immutable\n" +
                "src/aa/B.java:16: Error: you cannot invoke the [putAll(Collections.singletonMap(4, \"ll\"))] method on an immutable object. [ImmutableObject]\n" +
                "        field.putAll(Collections.singletonMap(4L, \"ll\")); //should lint\n" +
                "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:13: This reference is annotated by @Immutable\n" +
                "src/aa/B.java:20: Error: you cannot invoke the [put(5, \"aa\")] method on an immutable object. [ImmutableObject]\n" +
                "        field.put(5L, \"aa\"); //should lint\n" +
                "        ~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:13: This reference is annotated by @Immutable\n" +
                "6 errors, 0 warnings")
            .expectFixDiffs("Fix for src/aa/A.java line 9: Annotate [field] with @Immutable:\n" +
                "@@ -9 +9\n" +
                "-     private Map<Long, String> field; //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable private Map<Long, String> field; //should lint\n" +
                "Fix for src/aa/B.java line 13: Annotate [field] with @Immutable:\n" +
                "@@ -13 +13\n" +
                "-     Map<Long, String> field = new B().getA().a(); //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable Map<Long, String> field = new B().getA().a(); //should lint")
    }

    @Test
    fun `Check Java field @Immutable to field`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class A {

                    @Immutable
                    public final Map<Long, String> field;

                    @Immutable
                    public final Map<Long, String> field2 = new TreeMap<>();

                    private Map<Long, String> field3 = field2; //should lint

                    public final Map<Long, String> field4 = new TreeMap<>();

                    private Map<Long, String> field5 = field4; //should not lint

                    @Immutable
                    private Map<Long, String> field6 = field4; //should not lint

                    public A() {
                        Map<Long, String> local = new HashMap<>();
                        local.put(3L, "2");
                        field = local;
                    }
                }
            """.trimIndent()),
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class B {

                    public A getA() {
                        return new A();
                    }

                    Map<Long, String> field = getA().field; //should lint

                    Map<Long, String> field2 = getA().field2; //should lint

                    public void a() {
                        getA().field.putAll(Collections.singletonMap(4L, "ll")); //should lint
                    }
                }
            """.trimIndent()),
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;
                import java.util.*;

                public class C {

                    @Immutable
                    protected Map<String, String> map = new HashMap<String, String>() {
                        {
                            put("a", "b");
                        }
                    };

                    private Collection<String> list = map.values(); //should lint
                }
            """.trimIndent()),
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;
                import java.util.*;

                public class D {

                    @Immutable
                    protected Map<String, String> map = new HashMap<String, String>() {
                        {
                            put("a", "b");
                        }
                    };

                    private Iterator<String> iter = map.keySet().iterator(); //should lint
                }
            """.trimIndent()),
            java("""
                package aa;
                import java.utils.*;

                class E extends C {

                    private Map.Entry<String, String> entry = map.entrySet().iterator().next(); //should lint
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("src/aa/A.java:15: Error: Unable to assign an immutable object [field2] to a mutable field [field3]. [ImmutableEscape]\n" +
                "    private Map<Long, String> field3 = field2; //should lint\n" +
                "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:15: This expression [field2] is immutable.\n" +
                "src/aa/B.java:13: Error: Unable to assign an immutable object [getA().field] to a mutable field [field]. [ImmutableEscape]\n" +
                "    Map<Long, String> field = getA().field; //should lint\n" +
                "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:13: This expression [getA().field] is immutable.\n" +
                "src/aa/B.java:15: Error: Unable to assign an immutable object [getA().field2] to a mutable field [field2]. [ImmutableEscape]\n" +
                "    Map<Long, String> field2 = getA().field2; //should lint\n" +
                "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:15: This expression [getA().field2] is immutable.\n" +
                "src/aa/C.java:15: Error: Unable to assign an immutable object [map.values()] to a mutable field [list]. [ImmutableEscape]\n" +
                "    private Collection<String> list = map.values(); //should lint\n" +
                "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/C.java:15: This expression [map.values()] is immutable.\n" +
                "src/aa/D.java:15: Error: Unable to assign an immutable object [map.keySet().iterator()] to a mutable field [iter]. [ImmutableEscape]\n" +
                "    private Iterator<String> iter = map.keySet().iterator(); //should lint\n" +
                "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/D.java:15: This expression [map.keySet().iterator()] is immutable.\n" +
                "src/aa/E.java:6: Error: Unable to assign an immutable object [map.entrySet().iterator().next()] to a mutable field [entry]. [ImmutableEscape]\n" +
                "    private Map.Entry<String, String> entry = map.entrySet().iterator().next(); //should lint\n" +
                "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/E.java:6: This expression [map.entrySet().iterator().next()] is immutable.\n" +
                "src/aa/B.java:18: Error: you cannot invoke the [putAll(Collections.singletonMap(4, \"ll\"))] method on an immutable object. [ImmutableObject]\n" +
                "        getA().field.putAll(Collections.singletonMap(4L, \"ll\")); //should lint\n" +
                "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:9: This reference is annotated by @Immutable\n" +
                "7 errors, 0 warnings")
            .expectFixDiffs("Fix for src/aa/A.java line 15: Annotate [field3] with @Immutable:\n" +
                "@@ -15 +15\n" +
                "-     private Map<Long, String> field3 = field2; //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable private Map<Long, String> field3 = field2; //should lint\n" +
                "Fix for src/aa/B.java line 13: Annotate [field] with @Immutable:\n" +
                "@@ -13 +13\n" +
                "-     Map<Long, String> field = getA().field; //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable Map<Long, String> field = getA().field; //should lint\n" +
                "Fix for src/aa/B.java line 15: Annotate [field2] with @Immutable:\n" +
                "@@ -15 +15\n" +
                "-     Map<Long, String> field2 = getA().field2; //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable Map<Long, String> field2 = getA().field2; //should lint\n" +
                "Fix for src/aa/C.java line 15: Annotate [list] with @Immutable:\n" +
                "@@ -15 +15\n" +
                "-     private Collection<String> list = map.values(); //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable private Collection<String> list = map.values(); //should lint\n" +
                "Fix for src/aa/D.java line 15: Annotate [iter] with @Immutable:\n" +
                "@@ -15 +15\n" +
                "-     private Iterator<String> iter = map.keySet().iterator(); //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable private Iterator<String> iter = map.keySet().iterator(); //should lint\n" +
                "Fix for src/aa/E.java line 6: Annotate [entry] with @Immutable:\n" +
                "@@ -6 +6\n" +
                "-     private Map.Entry<String, String> entry = map.entrySet().iterator().next(); //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable private Map.Entry<String, String> entry = map.entrySet().iterator().next(); //should lint")
    }

    @Test
    fun `Check Java field @Immutable to method return`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class A {

                    @Immutable
                    public final Map<Long, String> field;

                    @Immutable
                    final TreeMap<Long, String> field2 = new TreeMap<>();

                    public final Map<Long, String> field3;

                    final TreeMap<Long, String> field4 = new TreeMap<>();

                    public A() {
                        Map<Long, String> local = new HashMap<>();
                        local.put(3L, "2");
                        field = local;
                        field3 = local;
                    }

                    public Map<Long, String> b() { //should lint
                        return field;
                    }

                    protected TreeMap<Long, String> c() { //should lint
                        return field2;
                    }

                    public Map<Long, String> d() { //should not lint
                        return field3;
                    }

                    public Map<Long, String> e() { //should not lint
                        return field4;
                    }
                }
            """.trimIndent()),
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class B extends A {

                    public A getA() {
                        return new A();
                    }

                    public Map<Long, String> f() { //should lint
                        return getA().field;
                    }

                    public Map<Long, String> g() { //should lint
                        return field2;
                    }

                    public Map<Long, String> h() { //should not lint
                        return getA().field3;
                    }

                    public Map<Long, String> i() { //should not lint
                        return field4;
                    }
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("src/aa/A.java:26: Error: Unable to return an immutable expression within a method without @Immutable annotation. [ImmutableEscape]\n" +
                "    public Map<Long, String> b() { //should lint\n" +
                "                             ~\n" +
                "    src/aa/A.java:27: This expression [return field;] is immutable.\n" +
                "src/aa/A.java:30: Error: Unable to return an immutable expression within a method without @Immutable annotation. [ImmutableEscape]\n" +
                "    protected TreeMap<Long, String> c() { //should lint\n" +
                "                                    ~\n" +
                "    src/aa/A.java:31: This expression [return field2;] is immutable.\n" +
                "src/aa/B.java:13: Error: Unable to return an immutable expression within a method without @Immutable annotation. [ImmutableEscape]\n" +
                "    public Map<Long, String> f() { //should lint\n" +
                "                             ~\n" +
                "    src/aa/B.java:14: This expression [return getA().field;] is immutable.\n" +
                "src/aa/B.java:17: Error: Unable to return an immutable expression within a method without @Immutable annotation. [ImmutableEscape]\n" +
                "    public Map<Long, String> g() { //should lint\n" +
                "                             ~\n" +
                "    src/aa/B.java:18: This expression [return field2;] is immutable.\n" +
                "4 errors, 0 warnings")
            .expectFixDiffs("Fix for src/aa/A.java line 26: Annotate method [b] with @Immutable:\n" +
                "@@ -26 +26\n" +
                "-     public Map<Long, String> b() { //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable public Map<Long, String> b() { //should lint\n" +
                "Fix for src/aa/A.java line 30: Annotate method [c] with @Immutable:\n" +
                "@@ -30 +30\n" +
                "-     protected TreeMap<Long, String> c() { //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable protected TreeMap<Long, String> c() { //should lint\n" +
                "Fix for src/aa/B.java line 13: Annotate method [f] with @Immutable:\n" +
                "@@ -13 +13\n" +
                "-     public Map<Long, String> f() { //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable public Map<Long, String> f() { //should lint\n" +
                "Fix for src/aa/B.java line 17: Annotate method [g] with @Immutable:\n" +
                "@@ -17 +17\n" +
                "-     public Map<Long, String> g() { //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable public Map<Long, String> g() { //should lint")
    }

    @Test
    fun `Check Java local @Immutable to method return`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class A {

                    @Immutable
                    protected List<String> list = new ArrayList<>();

                    public Set<String> a(){ //should lint
                        @Immutable
                        Set<String> set = new TreeSet<String>(){
                            {
                                add("33");
                                add("22");
                            }
                        };
                        return set;
                    }

                    public Set<String> b(){ //should not lint
                        Set<String> set = new TreeSet<String>(){
                            {
                                add("33");
                                add("22");
                            }
                        };
                        return set;
                    }

                    public List<String> c() { //should lint
                        List<String> local = list;
                        return local;
                    }
                }
            """.trimIndent()),
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class B extends A {

                    public List<String> d() { //should lint
                        List<String> local = list;
                        local.add("3"); //should lint
                        return local;
                    }

                    public List<String> e() { //should not lint
                        List<String> local = new ArrayList<>(list);
                        return local;
                    }
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("src/aa/A.java:12: Error: Unable to return an immutable expression within a method without @Immutable annotation. [ImmutableEscape]\n" +
                "    public Set<String> a(){ //should lint\n" +
                "                       ~\n" +
                "    src/aa/A.java:20: This expression [return set;] is immutable.\n" +
                "src/aa/A.java:33: Error: Unable to return an immutable expression within a method without @Immutable annotation. [ImmutableEscape]\n" +
                "    public List<String> c() { //should lint\n" +
                "                        ~\n" +
                "    src/aa/A.java:35: This expression [return local;] is immutable.\n" +
                "src/aa/B.java:9: Error: Unable to return an immutable expression within a method without @Immutable annotation. [ImmutableEscape]\n" +
                "    public List<String> d() { //should lint\n" +
                "                        ~\n" +
                "    src/aa/B.java:12: This expression [return local;] is immutable.\n" +
                "src/aa/B.java:11: Error: you cannot invoke the [add(\"3\")] method on an immutable object. [ImmutableObject]\n" +
                "        local.add(\"3\"); //should lint\n" +
                "        ~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:9: This reference is annotated by @Immutable\n" +
                "4 errors, 0 warnings")
            .expectFixDiffs("Fix for src/aa/A.java line 12: Annotate method [a] with @Immutable:\n" +
                "@@ -12 +12\n" +
                "-     public Set<String> a(){ //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable public Set<String> a(){ //should lint\n" +
                "Fix for src/aa/A.java line 33: Annotate method [c] with @Immutable:\n" +
                "@@ -33 +33\n" +
                "-     public List<String> c() { //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable public List<String> c() { //should lint\n" +
                "Fix for src/aa/B.java line 9: Annotate method [d] with @Immutable:\n" +
                "@@ -9 +9\n" +
                "-     public List<String> d() { //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable public List<String> d() { //should lint")
    }

    @Test
    fun `Check Java method @Immutable to method return`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class A {

                    @Immutable
                    public Map<Long, String> a() {
                        Map<Long, String> local = new HashMap<>();
                        local.put(3L, "2");
                        return local;
                    }

                    public Map<Long, String> b() { //should lint
                        return a();
                    }

                    protected Map<Long, String> c() { //should lint
                        Map<Long, String> local = a();
                        local.clear(); //should lint
                        return local;
                    }
                }
            """.trimIndent()),
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class B extends A {

                    public A getA() {
                        return new A();
                    }

                    public Map<Long, String> d() { //should lint
                        return a();
                    }

                    public Map<Long, String> e() { //should lint
                        Map<Long, String> local = getA().a();
                        local.clear(); //should lint
                        return local;
                    }

                    public Map<Long, String> f() { //should not lint
                        return c();
                    }
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("src/aa/A.java:16: Error: Unable to return an immutable expression within a method without @Immutable annotation. [ImmutableEscape]\n" +
                "    public Map<Long, String> b() { //should lint\n" +
                "                             ~\n" +
                "    src/aa/A.java:17: This expression [return a();] is immutable.\n" +
                "src/aa/A.java:20: Error: Unable to return an immutable expression within a method without @Immutable annotation. [ImmutableEscape]\n" +
                "    protected Map<Long, String> c() { //should lint\n" +
                "                                ~\n" +
                "    src/aa/A.java:23: This expression [return local;] is immutable.\n" +
                "src/aa/B.java:13: Error: Unable to return an immutable expression within a method without @Immutable annotation. [ImmutableEscape]\n" +
                "    public Map<Long, String> d() { //should lint\n" +
                "                             ~\n" +
                "    src/aa/B.java:14: This expression [return a();] is immutable.\n" +
                "src/aa/B.java:17: Error: Unable to return an immutable expression within a method without @Immutable annotation. [ImmutableEscape]\n" +
                "    public Map<Long, String> e() { //should lint\n" +
                "                             ~\n" +
                "    src/aa/B.java:20: This expression [return local;] is immutable.\n" +
                "src/aa/A.java:22: Error: you cannot invoke the [clear()] method on an immutable object. [ImmutableObject]\n" +
                "        local.clear(); //should lint\n" +
                "        ~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:21: This reference is annotated by @Immutable\n" +
                "src/aa/B.java:19: Error: you cannot invoke the [clear()] method on an immutable object. [ImmutableObject]\n" +
                "        local.clear(); //should lint\n" +
                "        ~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:18: This reference is annotated by @Immutable\n" +
                "6 errors, 0 warnings")
            .expectFixDiffs("Fix for src/aa/A.java line 16: Annotate method [b] with @Immutable:\n" +
                "@@ -16 +16\n" +
                "-     public Map<Long, String> b() { //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable public Map<Long, String> b() { //should lint\n" +
                "Fix for src/aa/A.java line 20: Annotate method [c] with @Immutable:\n" +
                "@@ -20 +20\n" +
                "-     protected Map<Long, String> c() { //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable protected Map<Long, String> c() { //should lint\n" +
                "Fix for src/aa/B.java line 13: Annotate method [d] with @Immutable:\n" +
                "@@ -13 +13\n" +
                "-     public Map<Long, String> d() { //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable public Map<Long, String> d() { //should lint\n" +
                "Fix for src/aa/B.java line 17: Annotate method [e] with @Immutable:\n" +
                "@@ -17 +17\n" +
                "-     public Map<Long, String> e() { //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable public Map<Long, String> e() { //should lint")
    }

    @Test
    fun `Check Java argument @Immutable to local`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class A {

                    public int a(@Immutable LinkedList<Integer> list) {
                        int ele = list.removeFirst(); //should lint
                        return ele;
                    }

                    public int b(@Immutable LinkedList<Integer> list) {
                        int ele = list.peekFirst(); //should not lint
                        return ele;
                    }

                    public int c(@Immutable LinkedList<Integer> list) {
                        return list.pollFirst(); //should lint
                    }

                    public int d(@Immutable LinkedList<Integer> list) {
                        return list.peek(); //should not lint
                    }

                    private void e(@Immutable HashMap<String, Long> map) {
                        Map<String, Long> local = map;
                        local.values().clear(); //should lint

                        local.put("haha", 3L); //should lint

                        for (String key : local.keySet()) {
                            key = key.replace("h", "a");
                        }
                        local = (HashMap<String, Long>) map.clone();
                    }

                    private void f(@Immutable HashMap<String, Long> map) {
                        map.values().clear(); //should lint

                        map.put("haha", 3L); //should lint

                        for (String key : map.keySet()) {
                            key = key.replace("h", "a");
                        }
                        map = (HashMap<String, Long>) map.clone();
                    }
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("src/aa/A.java:10: Error: you cannot invoke the [removeFirst()] method on an immutable object. [ImmutableObject]\n" +
                "        int ele = list.removeFirst(); //should lint\n" +
                "                  ~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:9: This reference is annotated by @Immutable\n" +
                "src/aa/A.java:20: Error: you cannot invoke the [pollFirst()] method on an immutable object. [ImmutableObject]\n" +
                "        return list.pollFirst(); //should lint\n" +
                "               ~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:19: This reference is annotated by @Immutable\n" +
                "src/aa/A.java:29: Error: you cannot invoke the [clear()] method on an immutable object. [ImmutableObject]\n" +
                "        local.values().clear(); //should lint\n" +
                "        ~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:27: This reference is annotated by @Immutable\n" +
                "src/aa/A.java:31: Error: you cannot invoke the [put(\"haha\", 3)] method on an immutable object. [ImmutableObject]\n" +
                "        local.put(\"haha\", 3L); //should lint\n" +
                "        ~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:27: This reference is annotated by @Immutable\n" +
                "src/aa/A.java:40: Error: you cannot invoke the [clear()] method on an immutable object. [ImmutableObject]\n" +
                "        map.values().clear(); //should lint\n" +
                "        ~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:39: This reference is annotated by @Immutable\n" +
                "src/aa/A.java:42: Error: you cannot invoke the [put(\"haha\", 3)] method on an immutable object. [ImmutableObject]\n" +
                "        map.put(\"haha\", 3L); //should lint\n" +
                "        ~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:39: This reference is annotated by @Immutable\n" +
                "6 errors, 0 warnings")
    }

    @Test
    fun `Check Java argument @Immutable to field`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class A {

                    private LinkedList<Integer> list; //should lint

                    @Immutable
                    protected LinkedList<Integer> list2;

                    private Map<String, Long> map = new HashMap<>(); //should lint

                    protected Set<String> set = new HashSet<>(); //should lint

                    public int a(@Immutable LinkedList<Integer> list) {
                        this.list = list;
                        return this.list.pollFirst();
                    }

                    public int b(@Immutable LinkedList<Integer> list) {
                        this.list2 = list; //should not lint
                        return 2;
                    }

                    private void e(@Immutable HashMap<String, Long> map) {
                        this.map.put("123", 123L);
                        this.map = map;
                    }

                    private void f(@Immutable HashMap<String, Long> map) {

                        this.set = new HashSet<>(map.keySet()); //should not lint

                        this.set = map.keySet();
                    }
                }
            """.trimIndent()),
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class B extends A {

                    private Iterator<String> global; //should lint

                    public void g(@Immutable LinkedList<Integer> list) {
                        this.list = list; // should lint
                    }

                    public void h(LinkedList<Integer> list) {
                        this.list = list; // should not lint
                    }

                    public void i(@Immutable LinkedList<Integer> list) {
                        this.list2 = list; // should not lint
                    }

                    private void j(@Immutable LinkedList<String> list) {
                        this.set = new HashSet<>(list); // should not lint
                    }

                    private void k(@Immutable Set<String> set) {
                        this.set = set; //should lint
                    }

                    private void l(@Immutable Set<String> set) {
                        Set<String> local = set;
                        global = local.iterator();
                    }
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("src/aa/A.java:9: Error: Unable to assign an immutable object [list] to a mutable field [list]. [ImmutableEscape]\n" +
                "    private LinkedList<Integer> list; //should lint\n" +
                "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:19: This expression [list] is immutable.\n" +
                "src/aa/A.java:9: Error: Unable to assign an immutable object [list] to a mutable field [list]. [ImmutableEscape]\n" +
                "    private LinkedList<Integer> list; //should lint\n" +
                "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:12: This expression [list] is immutable.\n" +
                "src/aa/A.java:14: Error: Unable to assign an immutable object [map] to a mutable field [map]. [ImmutableEscape]\n" +
                "    private Map<String, Long> map = new HashMap<>(); //should lint\n" +
                "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:30: This expression [map] is immutable.\n" +
                "src/aa/A.java:16: Error: Unable to assign an immutable object [map.keySet()] to a mutable field [set]. [ImmutableEscape]\n" +
                "    protected Set<String> set = new HashSet<>(); //should lint\n" +
                "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:37: This expression [map.keySet()] is immutable.\n" +
                "src/aa/A.java:16: Error: Unable to assign an immutable object [set] to a mutable field [set]. [ImmutableEscape]\n" +
                "    protected Set<String> set = new HashSet<>(); //should lint\n" +
                "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:28: This expression [set] is immutable.\n" +
                "src/aa/B.java:9: Error: Unable to assign an immutable object [local.iterator()] to a mutable field [global]. [ImmutableEscape]\n" +
                "    private Iterator<String> global; //should lint\n" +
                "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:33: This expression [local.iterator()] is immutable.\n" +
                "6 errors, 0 warnings")
            .expectFixDiffs("Fix for src/aa/A.java line 9: Annotate [list] with @Immutable:\n" +
                "@@ -9 +9\n" +
                "-     private LinkedList<Integer> list; //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable private LinkedList<Integer> list; //should lint\n" +
                "Fix for src/aa/A.java line 9: Annotate [list] with @Immutable:\n" +
                "@@ -9 +9\n" +
                "-     private LinkedList<Integer> list; //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable private LinkedList<Integer> list; //should lint\n" +
                "Fix for src/aa/A.java line 14: Annotate [map] with @Immutable:\n" +
                "@@ -14 +14\n" +
                "-     private Map<String, Long> map = new HashMap<>(); //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable private Map<String, Long> map = new HashMap<>(); //should lint\n" +
                "Fix for src/aa/A.java line 16: Annotate [set] with @Immutable:\n" +
                "@@ -16 +16\n" +
                "-     protected Set<String> set = new HashSet<>(); //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable protected Set<String> set = new HashSet<>(); //should lint\n" +
                "Fix for src/aa/A.java line 16: Annotate [set] with @Immutable:\n" +
                "@@ -16 +16\n" +
                "-     protected Set<String> set = new HashSet<>(); //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable protected Set<String> set = new HashSet<>(); //should lint\n" +
                "Fix for src/aa/B.java line 9: Annotate [global] with @Immutable:\n" +
                "@@ -9 +9\n" +
                "-     private Iterator<String> global; //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable private Iterator<String> global; //should lint")
    }

    @Test
    fun `Check Java argument @Immutable to method return`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class A {

                    public List<Integer> a(@Immutable LinkedList<Integer> list) { //should lint
                        return list;
                    }

                    public LinkedList<Integer> b(@Immutable LinkedList<Integer> list) {  //should lint
                        LinkedList<Integer> local = list;
                        for (Integer i : local) {
                            System.out.println(i);
                        }
                        return local;
                    }

                    private Set<String> c(Set<String> set) { //should not lint
                        return set;
                    }

                    @Immutable
                    private Set<String> d(@Immutable Set<String> set) { //should not lint
                        return set;
                    }

                    @Immutable
                    private Collection<String> d(@Immutable Map<Integer, String> set) { //should not lint
                        return set.values();
                    }

                    @Immutable
                    private Set<Integer> e(Map<Integer, String> set) { //should not lint
                        return set.keySet();
                    }
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("src/aa/A.java:9: Error: Unable to return an immutable expression within a method without @Immutable annotation. [ImmutableEscape]\n" +
                "    public List<Integer> a(@Immutable LinkedList<Integer> list) { //should lint\n" +
                "                         ~\n" +
                "    src/aa/A.java:10: This expression [return list;] is immutable.\n" +
                "src/aa/A.java:13: Error: Unable to return an immutable expression within a method without @Immutable annotation. [ImmutableEscape]\n" +
                "    public LinkedList<Integer> b(@Immutable LinkedList<Integer> list) {  //should lint\n" +
                "                               ~\n" +
                "    src/aa/A.java:18: This expression [return local;] is immutable.\n" +
                "2 errors, 0 warnings")
            .expectFixDiffs("Fix for src/aa/A.java line 9: Annotate method [a] with @Immutable:\n" +
                "@@ -9 +9\n" +
                "-     public List<Integer> a(@Immutable LinkedList<Integer> list) { //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable public List<Integer> a(@Immutable LinkedList<Integer> list) { //should lint\n" +
                "Fix for src/aa/A.java line 13: Annotate method [b] with @Immutable:\n" +
                "@@ -13 +13\n" +
                "-     public LinkedList<Integer> b(@Immutable LinkedList<Integer> list) {  //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable public LinkedList<Integer> b(@Immutable LinkedList<Integer> list) {  //should lint")
    }

    @Test
    fun `Check Java local @Immutable to argument`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class A {

                    public void a(@Immutable Collection<String> list) {
                        for (String a : list) {
                            System.out.println(a);
                        }
                    }

                    protected Long b(@Immutable Queue<Long> que) {
                        c(que); //should lint
                        return que.peek();
                    }

                    public void c(Queue<Long> queue) {
                        b(queue);
                        throw new UnsupportedOperationException("not implement");
                    }

                    public void d() {
                        @Immutable
                        Map<String, String> map = new HashMap<String, String>() {
                            {
                                put("a", "b");
                            }
                        };
                        Collection<String> list = map.values();

                        a(list); //should not lint

                        e(map); //should lint
                    }

                    public void e(Map<String, String> map) {
                        d();
                        System.out.println(map);
                    }
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("src/aa/A.java:20: Error: The method [c(que)] is called but this parameter [queue] is mutable. [ImmutableEscape]\n" +
                "    public void c(Queue<Long> queue) {\n" +
                "                  ~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:16: Unable to pass an immutable object [que] to a mutable parameter [void c(Queue<Long> queue))\n" +
                "src/aa/A.java:39: Error: The method [e(map)] is called but this parameter [map] is mutable. [ImmutableEscape]\n" +
                "    public void e(Map<String, String> map) {\n" +
                "                  ~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:36: Unable to pass an immutable object [map] to a mutable parameter [void e(Map<String, String> map))\n" +
                "2 errors, 0 warnings")
            .expectFixDiffs("Fix for src/aa/A.java line 20: Annotate parameter [Queue<Long> queue] with @Immutable:\n" +
                "@@ -20 +20\n" +
                "-     public void c(Queue<Long> queue) {\n" +
                "+     public void c(@com.yy.mobile.whisper.Immutable Queue<Long> queue) {\n" +
                "Fix for src/aa/A.java line 39: Annotate parameter [Map<String, String> map] with @Immutable:\n" +
                "@@ -39 +39\n" +
                "-     public void e(Map<String, String> map) {\n" +
                "+     public void e(@com.yy.mobile.whisper.Immutable Map<String, String> map) {")
    }

    @Test
    fun `Check Java field @Immutable to argument`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class A {

                    @Immutable
                    protected Map<String, String> map = new HashMap<String, String>() {
                        {
                            put("a", "b");
                        }
                    };

                    Collection<String> list = map.values(); //should lint

                    public void a(@Immutable Collection<String> list) {
                        for (String a : list) {
                            System.out.println(a);
                        }
                        b(map); //should lint
                    }

                    public void b(Map<String, String> map) {
                        c(this.map);
                        throw new RuntimeException(map.keySet().toString());
                    }

                    public void c(@Immutable Map<? extends CharSequence, String> map) {
                        a(map.values());
                        a(list);
                    }
                }
            """.trimIndent())
            ,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class B extends A {

                    public void d(Map<String, ? extends CharSequence> map) {
                        b(this.map); //should lint
                        e(this.map.values());
                        e(map.values());
                    }

                    public void e(@Immutable Collection<? extends CharSequence> collection) {
                        d(this.map); //should lint
                        d(new HashMap<>(this.map));
                    }
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("src/aa/A.java:16: Error: Unable to assign an immutable object [map.values()] to a mutable field [list]. [ImmutableEscape]\n" +
                "    Collection<String> list = map.values(); //should lint\n" +
                "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:16: This expression [map.values()] is immutable.\n" +
                "src/aa/A.java:25: Error: The method [b(map)] is called but this parameter [map] is mutable. [ImmutableEscape]\n" +
                "    public void b(Map<String, String> map) {\n" +
                "                  ~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:22: Unable to pass an immutable object [map] to a mutable parameter [void b(Map<String, String> map))\n" +
                "src/aa/A.java:25: Error: The method [b(this.map)] is called but this parameter [map] is mutable. [ImmutableEscape]\n" +
                "    public void b(Map<String, String> map) {\n" +
                "                  ~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:10: Unable to pass an immutable object [this.map] to a mutable parameter [void b(Map<String, String> map))\n" +
                "src/aa/B.java:9: Error: The method [d(this.map)] is called but this parameter [map] is mutable. [ImmutableEscape]\n" +
                "    public void d(Map<String, ? extends CharSequence> map) {\n" +
                "                  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:16: Unable to pass an immutable object [this.map] to a mutable parameter [void d(Map<String, ? extends CharSequence> map))\n" +
                "4 errors, 0 warnings")
            .expectFixDiffs("Fix for src/aa/A.java line 16: Annotate [list] with @Immutable:\n" +
                "@@ -16 +16\n" +
                "-     Collection<String> list = map.values(); //should lint\n" +
                "+     @com.yy.mobile.whisper.Immutable Collection<String> list = map.values(); //should lint\n" +
                "Fix for src/aa/A.java line 25: Annotate parameter [Map<String, String> map] with @Immutable:\n" +
                "@@ -25 +25\n" +
                "-     public void b(Map<String, String> map) {\n" +
                "+     public void b(@com.yy.mobile.whisper.Immutable Map<String, String> map) {\n" +
                "Fix for src/aa/A.java line 25: Annotate parameter [Map<String, String> map] with @Immutable:\n" +
                "@@ -25 +25\n" +
                "-     public void b(Map<String, String> map) {\n" +
                "+     public void b(@com.yy.mobile.whisper.Immutable Map<String, String> map) {\n" +
                "Fix for src/aa/B.java line 9: Annotate parameter [Map<String, ? extends CharSequence> map] with @Immutable:\n" +
                "@@ -9 +9\n" +
                "-     public void d(Map<String, ? extends CharSequence> map) {\n" +
                "+     public void d(@com.yy.mobile.whisper.Immutable Map<String, ? extends CharSequence> map) {")
    }

    @Test
    fun `Check Java method return @Immutable to argument`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class A {

                    @Immutable
                    public List<String> a() {
                        return new ArrayList<>();
                    }

                    public void b(List<String> list) { //should lint
                        Log.i("haha", "list = " + list);
                    }

                    private void c() {
                        b(a()); //lint
                    }

                    protected void c(List<String> list) { //should lint
                        d();

                        e(list);
                    }

                    public void d() {
                        List<String> list = a();
                        if (list instanceof ArrayList) {
                            b(list); //lint

                            e(list);
                        }
                    }

                    private void e(@Immutable List<String> list) {
                        c();
                    }
                }
            """.trimIndent()),
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class B extends A {

                    public void e() {
                        c(a()); //lint

                        Collection<String> values = newMap().values();
                        b(new ArrayList<>(values));
                    }

                    @Immutable
                    public Map<String, String> newMap() {
                        e();
                        return new TreeMap<>();
                    }
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("src/aa/A.java:14: Error: The method [b(a())] is called but this parameter [list] is mutable. [ImmutableEscape]\n" +
                "    public void b(List<String> list) { //should lint\n" +
                "                  ~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:19: Unable to pass an immutable object [a()] to a mutable parameter [void b(List<String> list))\n" +
                "src/aa/A.java:14: Error: The method [b(list)] is called but this parameter [list] is mutable. [ImmutableEscape]\n" +
                "    public void b(List<String> list) { //should lint\n" +
                "                  ~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/A.java:31: Unable to pass an immutable object [list] to a mutable parameter [void b(List<String> list))\n" +
                "src/aa/A.java:22: Error: The method [c(a())] is called but this parameter [list] is mutable. [ImmutableEscape]\n" +
                "    protected void c(List<String> list) { //should lint\n" +
                "                     ~~~~~~~~~~~~~~~~~\n" +
                "    src/aa/B.java:10: Unable to pass an immutable object [a()] to a mutable parameter [void c(List<String> list))\n" +
                "3 errors, 0 warnings")
            .expectFixDiffs("Fix for src/aa/A.java line 14: Annotate parameter [List<String> list] with @Immutable:\n" +
                "@@ -14 +14\n" +
                "-     public void b(List<String> list) { //should lint\n" +
                "+     public void b(@com.yy.mobile.whisper.Immutable List<String> list) { //should lint\n" +
                "Fix for src/aa/A.java line 14: Annotate parameter [List<String> list] with @Immutable:\n" +
                "@@ -14 +14\n" +
                "-     public void b(List<String> list) { //should lint\n" +
                "+     public void b(@com.yy.mobile.whisper.Immutable List<String> list) { //should lint\n" +
                "Fix for src/aa/A.java line 22: Annotate parameter [List<String> list] with @Immutable:\n" +
                "@@ -22 +22\n" +
                "-     protected void c(List<String> list) { //should lint\n" +
                "+     protected void c(@com.yy.mobile.whisper.Immutable List<String> list) { //should lint")
    }

    @Test
    fun `Check Java method or argument @Immutable to override`() {

        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class A {

                    @Immutable
                    public List<String> a() {
                        return new ArrayList<>();
                    }

                    public List<String> b() {
                        return new LinkedList<>();
                    }

                    @Immutable
                    protected List<String> c() {
                        return a();
                    }

                    public void d(@Immutable List<String> a) {
                        //do nothing
                    }

                    public void e(List<String> a) {
                        //do nothing
                    }

                    void f(List<String> a) {
                        //do nothing
                    }

                    @Immutable
                    private Set<String> g(@Immutable Set<String> set){
                        return set;
                    }
                }
            """.trimIndent()),
            java("""
                package aa;

                import com.yy.mobile.whisper.Immutable;

                import java.util.*;

                public class B extends A {

                    @Override
                    public List<String> a() { //should lint
                        return super.a();
                    }

                    @Override
                    public List<String> b() { //should not lint
                        return super.b();
                    }

                    @Override
                    protected List<String> c() { //should lint
                        return super.c();
                    }

                    @Override
                    public void d(List<String> a) { //should lint
                        super.d(a);
                    }

                    @Override
                    public void e(List<String> a) { //should not lint
                        super.e(a);
                    }

                    @Override
                    void f(List<String> a) { //should not lint
                        super.f(a);
                    }

                    private Set<String> g(Set<String> set) { //should not lint
                        return set;
                    }
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("src/aa/B.java:10: Error: Unable to return an immutable expression within a method without @Immutable annotation. [ImmutableEscape]\n" +
                "    public List<String> a() { //should lint\n" +
                "                        ~\n" +
                "    src/aa/B.java:11: This expression [return super.a();] is immutable.\n" +
                "src/aa/B.java:20: Error: Unable to return an immutable expression within a method without @Immutable annotation. [ImmutableEscape]\n" +
                "    protected List<String> c() { //should lint\n" +
                "                           ~\n" +
                "    src/aa/B.java:21: This expression [return super.c();] is immutable.\n" +
                "src/aa/B.java:10: Error: The method [java.util.List<java.lang.String> a()] without @Immutable cannot override @Immutable method. [ImmutableOverride]\n" +
                "    public List<String> a() { //should lint\n" +
                "                        ~\n" +
                "src/aa/B.java:20: Error: The method [java.util.List<java.lang.String> c()] without @Immutable cannot override @Immutable method. [ImmutableOverride]\n" +
                "    protected List<String> c() { //should lint\n" +
                "                           ~\n" +
                "src/aa/B.java:25: Error: The parameter [List<String> a] without @Immutable cannot override @Immutable parameter. [ImmutableOverride]\n" +
                "    public void d(List<String> a) { //should lint\n" +
                "                  ~~~~~~~~~~~~~~\n" +
                "5 errors, 0 warnings")
            .expectFixDiffs("Fix for src/aa/B.java line 10: Annotate method [a] with @Immutable:\n" +
                "@@ -9 +9\n" +
                "-     @Override\n" +
                "+     @com.yy.mobile.whisper.Immutable @Override\n" +
                "Fix for src/aa/B.java line 20: Annotate method [c] with @Immutable:\n" +
                "@@ -19 +19\n" +
                "-     @Override\n" +
                "+     @com.yy.mobile.whisper.Immutable @Override\n" +
                "Fix for src/aa/B.java line 10: add @Immutable annotation:\n" +
                "@@ -9 +9\n" +
                "-     @Override\n" +
                "+     @com.yy.mobile.whisper.Immutable @Override\n" +
                "Fix for src/aa/B.java line 20: add @Immutable annotation:\n" +
                "@@ -19 +19\n" +
                "-     @Override\n" +
                "+     @com.yy.mobile.whisper.Immutable @Override\n" +
                "Fix for src/aa/B.java line 25: add @Immutable annotation:\n" +
                "@@ -25 +25\n" +
                "-     public void d(List<String> a) { //should lint\n" +
                "+     public void d(@com.yy.mobile.whisper.Immutable List<String> a) { //should lint")
    }

    @Test
    fun `Check Java wrong type for @Immutable`(){
        TestLintTask.lint().files(
            immutableAnnotation,
            java("""
                package aa;

                import android.support.annotation.Nullable;

                import com.yy.mobile.whisper.Immutable;

                import java.util.concurrent.ConcurrentLinkedDeque;

                public class A {

                    @Immutable
                    private int a = 3;

                    @Immutable
                    private final StringBuilder sb = new StringBuilder();

                    @Nullable
                    @Immutable
                    private final ConcurrentLinkedDeque a() {
                        return null;
                    }

                    @Immutable
                    private final void b() {
                    }
                }
            """.trimIndent()))
            .detector(WhisperImmutableDetector())
            .run()
            .expect("src/aa/A.java:12: Warning: Only class [java.util.Collection, java.util.Map, java.util.Map.Entry, java.util.Iterator] or their subclass can be annotated by @Immutable. \n" +
                "but current is [int] [ImmutableTarget]\n" +
                "    private int a = 3;\n" +
                "                ~\n" +
                "src/aa/A.java:15: Warning: Only class [java.util.Collection, java.util.Map, java.util.Map.Entry, java.util.Iterator] or their subclass can be annotated by @Immutable. \n" +
                "but current is [java.lang.StringBuilder] [ImmutableTarget]\n" +
                "    private final StringBuilder sb = new StringBuilder();\n" +
                "                                ~~\n" +
                "src/aa/A.java:24: Warning: Only class [java.util.Collection, java.util.Map, java.util.Map.Entry, java.util.Iterator] or their subclass can be annotated by @Immutable. \n" +
                "but current is [void] [ImmutableTarget]\n" +
                "    private final void b() {\n" +
                "                       ~\n" +
                "0 errors, 3 warnings")
    }
}