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

package proton.android.pass.autofill.autofillhealth.troubleshooting

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.AutofillDiagnosticsBuilder
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.AutofillDiagnosticsInput
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.BrowserAutofillCoverage
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.BrowserInfo

class AutofillDiagnosticsBuilderTest {

    @Test
    fun `builds a non-sensitive summary`() {
        val text = AutofillDiagnosticsBuilder.build(
            AutofillDiagnosticsInput(
                isDefaultAutofillService = false,
                androidSdkInt = 34,
                deviceModel = "Pixel 8",
                appVersion = "1.40.0",
                installedBrowsers = listOf(
                    BrowserInfo("com.android.chrome", "Chrome", BrowserAutofillCoverage.Ready),
                    BrowserInfo("org.mozilla.firefox", "Firefox", BrowserAutofillCoverage.NeedsSetup)
                ),
                supportsInlineSuggestions = true
            )
        )

        assertThat(text).contains("Default autofill service: no")
        assertThat(text).contains("Android SDK: 34")
        assertThat(text).contains("Device: Pixel 8")
        assertThat(text).contains("App: 1.40.0")
        assertThat(text).contains("com.android.chrome (ready)")
        assertThat(text).contains("org.mozilla.firefox (needs setup)")
        assertThat(text).contains("Inline suggestions supported: yes")
    }

    @Test
    fun `handles no installed browsers`() {
        val text = AutofillDiagnosticsBuilder.build(
            AutofillDiagnosticsInput(
                isDefaultAutofillService = true,
                androidSdkInt = 30,
                deviceModel = "Device",
                appVersion = "1.0.0",
                installedBrowsers = emptyList(),
                supportsInlineSuggestions = false
            )
        )

        assertThat(text).contains("Default autofill service: yes")
        assertThat(text).contains("(none detected)")
    }
}
