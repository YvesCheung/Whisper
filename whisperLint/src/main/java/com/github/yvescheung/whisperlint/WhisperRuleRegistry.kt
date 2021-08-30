package com.github.yvescheung.whisperlint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

@Suppress("UnstableApiUsage")
class WhisperRuleRegistry : IssueRegistry() {

    override val issues: List<Issue>
        get() = listOf(
            *WhisperHintDetector.getIssue(),
            *WhisperDeprecatedDetector.getIssue(),
            *WhisperHideDetector.getIssue(),
            *WhisperUseWithDetector.getIssue(),
            *WhisperImmutableDetector.getIssue(),
            *WhisperConstDefDetector.getIssue(),
            *WhisperRangeDetector.getIssue()
        )

    override val api: Int = CURRENT_API

    override val minApi: Int = 1
}