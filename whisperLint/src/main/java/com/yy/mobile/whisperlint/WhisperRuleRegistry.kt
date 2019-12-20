package com.yy.mobile.whisperlint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

class WhisperRuleRegistry : IssueRegistry() {

    override val issues: List<Issue>
        get() = listOf(
            *WhisperHintDetector.getIssue(),
            *WhisperDeprecatedDetector.getIssue(),
            *WhisperHideDetector.getIssue(),
            *WhisperUseWithDetector.getIssue(),
            *WhisperImmutableDetector.getIssue())

    override val api: Int = CURRENT_API

    override val minApi: Int = 5
}