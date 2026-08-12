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

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.BrowserAutofillCoverage
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.BrowserInfo
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.CurrentAutofillServiceProvider
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillServiceStatus
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillTroubleshootingViewModel
import proton.android.pass.appconfig.fakes.FakeAppConfig
import proton.android.pass.preferences.AutofillDisplayPreference
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.test.MainDispatcherRule

class AutofillTroubleshootingViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private class FakeAutofillManager(
        private val status: AutofillSupportedStatus
    ) : AutofillManager {
        override fun getAutofillStatus(): Flow<AutofillSupportedStatus> = MutableStateFlow(status)
        override fun openAutofillSelector() = Unit
        override fun disableAutofill() = Unit
    }

    private class FakeCurrentAutofillServiceProvider(
        private val label: String?
    ) : CurrentAutofillServiceProvider {
        override fun getActiveOtherServiceLabel(): String? = label
    }

    private fun createViewModel(
        status: AutofillSupportedStatus,
        provider: FakeInstalledBrowsersProvider,
        sdkInt: Int,
        conflictingServiceLabel: String? = null,
        autofillDisplay: AutofillDisplayPreference = AutofillDisplayPreference.Inline
    ) = AutofillTroubleshootingViewModel(
        autofillManager = FakeAutofillManager(status),
        installedBrowsersProvider = provider,
        currentAutofillServiceProvider = FakeCurrentAutofillServiceProvider(conflictingServiceLabel),
        userPreferencesRepository = FakePreferenceRepository().apply {
            setAutofillDisplayPreference(autofillDisplay)
        },
        appConfig = FakeAppConfig().apply { setAndroidVersion(sdkInt) }
    )

    @Test
    fun `maps EnabledByOurService and exposes browsers and inline support`() = runTest {
        val browsers = listOf(
            BrowserInfo("com.android.chrome", "Chrome", BrowserAutofillCoverage.Ready)
        )
        val provider = FakeInstalledBrowsersProvider().apply { setBrowsers(browsers) }

        val viewModel = createViewModel(
            status = AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOurService),
            provider = provider,
            sdkInt = 34
        )

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.serviceStatus).isEqualTo(AutofillServiceStatus.EnabledByOurService)
            assertThat(state.installedBrowsers).isEqualTo(browsers)
            assertThat(state.supportsInlineSuggestions).isTrue()
        }
    }

    @Test
    fun `maps Disabled to NotDefault and no inline support below API 30`() = runTest {
        val viewModel = createViewModel(
            status = AutofillSupportedStatus.Supported(AutofillStatus.Disabled),
            provider = FakeInstalledBrowsersProvider(),
            sdkInt = 28
        )

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.serviceStatus).isEqualTo(AutofillServiceStatus.NotDefault)
            assertThat(state.supportsInlineSuggestions).isFalse()
        }
    }

    @Test
    fun `maps EnabledByOtherService to NotDefault`() = runTest {
        val viewModel = createViewModel(
            status = AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOtherService),
            provider = FakeInstalledBrowsersProvider(),
            sdkInt = 34
        )

        viewModel.state.test {
            assertThat(awaitItem().serviceStatus).isEqualTo(AutofillServiceStatus.NotDefault)
        }
    }

    @Test
    fun `exposes the conflicting autofill service label`() = runTest {
        val viewModel = createViewModel(
            status = AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOtherService),
            provider = FakeInstalledBrowsersProvider(),
            sdkInt = 34,
            conflictingServiceLabel = "Google"
        )

        viewModel.state.test {
            assertThat(awaitItem().conflictingServiceLabel).isEqualTo("Google")
        }
    }

    @Test
    fun `flags inline suggestions as enabled when display preference is Inline`() = runTest {
        val viewModel = createViewModel(
            status = AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOurService),
            provider = FakeInstalledBrowsersProvider(),
            sdkInt = 34,
            autofillDisplay = AutofillDisplayPreference.Inline
        )

        viewModel.state.test {
            assertThat(awaitItem().isInlineSuggestionsEnabled).isTrue()
        }
    }

    @Test
    fun `flags inline suggestions as disabled when display preference is Popup`() = runTest {
        val viewModel = createViewModel(
            status = AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOurService),
            provider = FakeInstalledBrowsersProvider(),
            sdkInt = 34,
            autofillDisplay = AutofillDisplayPreference.Popup
        )

        viewModel.state.test {
            assertThat(awaitItem().isInlineSuggestionsEnabled).isFalse()
        }
    }

    @Test
    fun `maps Unsupported`() = runTest {
        val viewModel = createViewModel(
            status = AutofillSupportedStatus.Unsupported,
            provider = FakeInstalledBrowsersProvider(),
            sdkInt = 34
        )

        viewModel.state.test {
            assertThat(awaitItem().serviceStatus).isEqualTo(AutofillServiceStatus.Unsupported)
        }
    }
}
