package hint.demo;

import com.yy.mobile.whisper.NeedError;
import com.yy.mobile.whisper.NeedInfo;
import com.yy.mobile.whisper.NeedWarning;

/**
 * @author YvesCheung
 * 2019-12-19
 */
public class ClassTest {

    public static void main(String[] args) {
        ClassTest instance = new ClassTest();
        instance.info();
        instance.warning();
        instance.error();
    }

    @NeedInfo("This method should show info")
    private void info() {
        //Do Nothing.
    }

    @NeedWarning("This method should show warning")
    private void warning() {
        //Do Nothing.
    }

    @NeedError("This method should show error")
    private void error() {
        //Do Nothing.
    }
}
