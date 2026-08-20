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
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.BrowserAutofillCoverage
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.BrowserInfo
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillHealthVerdict
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillHealthVerdictResolver
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillServiceStatus
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillTroubleshootingState

class AutofillHealthVerdictResolverTest {

    @Test
    fun `loading while the service status is loading`() {
        val state = AutofillTroubleshootingState(serviceStatus = AutofillServiceStatus.Loading)
        assertThat(AutofillHealthVerdictResolver.resolve(state))
            .isEqualTo(AutofillHealthVerdict.Loading)
    }

    @Test
    fun `unsupported when the device does not support autofill`() {
        val state = AutofillTroubleshootingState(serviceStatus = AutofillServiceStatus.Unsupported)
        assertThat(AutofillHealthVerdictResolver.resolve(state))
            .isEqualTo(AutofillHealthVerdict.Unsupported)
    }

    @Test
    fun `needs attention when Proton Pass is not the default service`() {
        val state = AutofillTroubleshootingState(serviceStatus = AutofillServiceStatus.NotDefault)
        assertThat(AutofillHealthVerdictResolver.resolve(state))
            .isEqualTo(AutofillHealthVerdict.NeedsAttention)
    }

    @Test
    fun `needs attention when a browser still needs setup`() {
        val state = AutofillTroubleshootingState(
            serviceStatus = AutofillServiceStatus.EnabledByOurService,
            installedBrowsers = listOf(
                BrowserInfo(
                    packageName = "com.android.chrome",
                    label = "Chrome",
                    coverage = BrowserAutofillCoverage.NeedsSetup,
                    canOpenAutofillSettings = true
                )
            )
        )
        assertThat(AutofillHealthVerdictResolver.resolve(state))
            .isEqualTo(AutofillHealthVerdict.NeedsAttention)
    }

    @Test
    fun `all good when the browser needing setup cannot be set up from here`() {
        val state = AutofillTroubleshootingState(
            serviceStatus = AutofillServiceStatus.EnabledByOurService,
            installedBrowsers = listOf(
                BrowserInfo("org.mozilla.firefox", "Firefox", BrowserAutofillCoverage.NeedsSetup)
            )
        )
        assertThat(AutofillHealthVerdictResolver.resolve(state))
            .isEqualTo(AutofillHealthVerdict.AllGood)
    }

    @Test
    fun `all good when service is enabled and no browser needs setup`() {
        val state = AutofillTroubleshootingState(
            serviceStatus = AutofillServiceStatus.EnabledByOurService,
            installedBrowsers = listOf(
                BrowserInfo("com.android.chrome", "Chrome", BrowserAutofillCoverage.Ready),
                BrowserInfo("com.brave.browser", "Brave", BrowserAutofillCoverage.Working)
            )
        )
        assertThat(AutofillHealthVerdictResolver.resolve(state))
            .isEqualTo(AutofillHealthVerdict.AllGood)
    }
}
