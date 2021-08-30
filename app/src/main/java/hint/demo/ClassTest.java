package hint.demo;

import com.github.yvescheung.whisper.NeedError;
import com.github.yvescheung.whisper.NeedInfo;
import com.github.yvescheung.whisper.NeedWarning;

/**
 * @author YvesCheung
 * 2019-12-19
 */
public class ClassTest {

    public static void main(String[] args) {
        ClassTest instance = new ClassTest();

        instance.method1();

        instance.method2();

        instance.method3();
    }

    @NeedInfo("Info: This is method1")
    private void method1() {
        //Do Nothing.
    }

    @NeedWarning("Warning: This is method2!")
    private void method2() {
        //Do Nothing.
    }

    @NeedError("Error: This is method3!")
    private void method3() {
        //Do Nothing.
    }
}
