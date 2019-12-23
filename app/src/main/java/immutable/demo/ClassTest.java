package immutable.demo;

import com.yy.mobile.whisper.Immutable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YvesCheung
 * 2019-12-23
 */
public class ClassTest {

    @Immutable
    private final List<String> list = new ArrayList<>();

    void test() {
        list.add("Can't add element to immutable collection");

        for (String element : list) {
            System.out.println("read immutable collection is ok");
        }
    }

    public List<String> escape() {
        return list;
    }
}
