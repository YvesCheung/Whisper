package usewith.demo;

import com.yy.mobile.whisper.UseWith;

/**
 * Created by 张宇 on 2018/9/19.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
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
