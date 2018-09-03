package com.yy.mobile;

import com.yy.mobile.whisperexample.TheClassShouldBeHide;

/**
 * Created by 张宇 on 2018/9/3.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
public class Asd {

    public Asd() {
        //new TheClassShouldBeHide().method();
    }

    public static class Asdd {

        public void method() {
            int shouldNotBeHide = new TheClassShouldBeHide().method();
        }
    }
}
