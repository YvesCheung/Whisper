package com.yy.mobile;

import com.yy.mobile.whisperexample.TheClassShouldBeHide;

/**
 * Created by 张宇 on 2018/9/3.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
public class NewActivity {

    public static class InnerNewActivity {

        private TheClassShouldBeHide field = new TheClassShouldBeHide();

        public void method3() {
            field.method();
        }

        private class InnerInnerNewActivity {

            public void method4() {
                field.method();
            }
        }
    }
}
