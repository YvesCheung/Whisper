package immutable.demo;

import com.github.yvescheung.whisper.Immutable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YvesCheung
 * 2019-12-23
 */
@SuppressWarnings("unused")
public class ClassTest {

    @Immutable
    private final List<String> immutableList = new ArrayList<>();

    private final List<String> mutableList = new ArrayList<>();

    void test() {
        immutableList.add("Can't add element to immutable collection");

        mutableList.add("Can add element to mutable collection");

        for (String element : immutableList) {
            System.out.println("read immutable collection is ok");
        }
        for (String element : mutableList) {
            System.out.println("read mutable collection is ok");
        }
    }

    @Immutable
    public List<String> escape() {
        return immutableList;
    }
}
