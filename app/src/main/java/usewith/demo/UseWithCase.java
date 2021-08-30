package usewith.demo;

import com.github.yvescheung.whisper.UseWith;

/**
 * @author YvesCheung
 * 2018/9/19.
 *
 */
public class UseWithCase {

    @UseWith("deInit")
    public void init() {

    }

    public void deInit() {

    }

    @UseWith("removeListener")
    public void addListener(Listener listener) {

    }

    public void removeListener(Listener listener) {

    }

    public interface Listener {
        void doSomething();
    }
}
