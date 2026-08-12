/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.autofill.autofillhealth.troubleshooting.data

import androidx.annotation.StringRes
import proton.android.pass.autofill.service.R

object BrowserAutofillInstructions {

    @StringRes
    private val genericStepsResId: Int = R.string.autofill_troubleshooting_browser_steps_generic

    private val knownSteps: Map<String, Int> = mapOf(
        "com.android.chrome" to R.string.autofill_troubleshooting_browser_steps_chrome,
        "com.chrome.beta" to R.string.autofill_troubleshooting_browser_steps_chrome,
        "com.chrome.dev" to R.string.autofill_troubleshooting_browser_steps_chrome,
        "com.brave.browser" to R.string.autofill_troubleshooting_browser_steps_chrome,
        "com.microsoft.emmx" to R.string.autofill_troubleshooting_browser_steps_edge,
        "com.microsoft.emmx.beta" to R.string.autofill_troubleshooting_browser_steps_edge,
        "com.microsoft.emmx.dev" to R.string.autofill_troubleshooting_browser_steps_edge,
        "com.microsoft.emmx.canary" to R.string.autofill_troubleshooting_browser_steps_edge,
        "com.opera.browser" to R.string.autofill_troubleshooting_browser_steps_chrome,
        "com.kiwibrowser.browser" to R.string.autofill_troubleshooting_browser_steps_chrome,
        "org.mozilla.firefox" to R.string.autofill_troubleshooting_browser_steps_firefox,
        "org.mozilla.firefox_beta" to R.string.autofill_troubleshooting_browser_steps_firefox,
        "org.mozilla.fenix" to R.string.autofill_troubleshooting_browser_steps_firefox,
        "com.sec.android.app.sbrowser" to R.string.autofill_troubleshooting_browser_steps_samsung
    )

    @StringRes
    fun stepsResIdFor(packageName: String): Int = knownSteps[packageName] ?: genericStepsResId
}
