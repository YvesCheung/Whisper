package com.yy.mobile.whisperlint;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Severity;

/**
 * Created by 张宇 on 2018/9/18.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 * <p>
 * Compatible with low version Gradle,
 * unable to use Kotlin to call Issue.create
 */
public class IssueFactory {

    public static Issue create(
            String id,
            String briefDescription,
            String explanation,
            Category category,
            int priority,
            Severity severity,
            Implementation implementation
    ) {
        return Issue.create(
                id,
                briefDescription,
                explanation,
                category,
                priority,
                severity,
                implementation);
    }
}
