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

data class AutofillDiagnosticsInput(
    val isDefaultAutofillService: Boolean,
    val androidSdkInt: Int,
    val deviceModel: String,
    val appVersion: String,
    val installedBrowsers: List<BrowserInfo>,
    val supportsInlineSuggestions: Boolean
)

object AutofillDiagnosticsBuilder {

    fun build(input: AutofillDiagnosticsInput): String = buildString {
        appendLine("Proton Pass — autofill diagnostics")
        appendLine("Default autofill service: ${input.isDefaultAutofillService.yesNo()}")
        appendLine("Android SDK: ${input.androidSdkInt}")
        appendLine("Device: ${input.deviceModel}")
        appendLine("App: ${input.appVersion}")
        appendLine("Inline suggestions supported: ${input.supportsInlineSuggestions.yesNo()}")
        appendLine("Installed browsers:")
        if (input.installedBrowsers.isEmpty()) {
            appendLine("  (none detected)")
        } else {
            input.installedBrowsers.forEach { browser ->
                appendLine("  - ${browser.packageName} (${browser.coverage.label()})")
            }
        }
    }.trimEnd()

    private fun Boolean.yesNo(): String = if (this) "yes" else "no"

    private fun BrowserAutofillCoverage.label(): String = when (this) {
        BrowserAutofillCoverage.Working -> "working"
        BrowserAutofillCoverage.Ready -> "ready"
        BrowserAutofillCoverage.NeedsSetup -> "needs setup"
    }
}
