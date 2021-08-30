package com.github.yvescheung.whisperlint.support

import com.android.tools.lint.detector.api.CURRENT_API

/**
 * @author YvesCheung
 * 2019-12-20
 */
internal object VersionChecker {

    private const val UNDEFINED = -1

    private var environmentApi = UNDEFINED

    fun envVersion(): Int {
        if (environmentApi != UNDEFINED) {
            return environmentApi
        }
        if (environmentApi == UNDEFINED) {
            try {
                val api = Class.forName("com.android.tools.lint.detector.api.ApiKt")
                val field = api.getDeclaredField("CURRENT_API")
                environmentApi = field.get(null) as Int
            } catch (e: Throwable) {
                //ignore
            }
        }

        if (environmentApi == UNDEFINED) {
            try {
                val api = Class.forName("com.android.tools.lint.detector.api.Api")
                val field = api.getDeclaredField("CURRENT_API")
                environmentApi = field.get(null) as Int
            } catch (e: Throwable) {
                //ignore
            }
        }

        if (environmentApi == UNDEFINED) {
            environmentApi = CURRENT_API
        }

        return environmentApi
    }
}