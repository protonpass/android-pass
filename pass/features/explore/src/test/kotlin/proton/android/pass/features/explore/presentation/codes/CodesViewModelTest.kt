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

package proton.android.pass.features.explore.presentation.codes

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.clipboard.fakes.FakeClipboardManager
import proton.android.pass.data.api.usecases.LoginTotpEntry
import proton.android.pass.data.fakes.usecases.FakeObserveLoginTotpEntries
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.preferences.UseFaviconsPreference
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.totp.fakes.FakeTotpManager

internal class CodesViewModelTest {

    @get:Rule
    internal val dispatcher = MainDispatcherRule()

    private lateinit var observeLoginTotpEntries: FakeObserveLoginTotpEntries
    private lateinit var preferenceRepository: FakePreferenceRepository

    private lateinit var instance: CodesViewModel

    @Before
    fun setup() {
        observeLoginTotpEntries = FakeObserveLoginTotpEntries().apply { emit(listOf(entry())) }
        preferenceRepository = FakePreferenceRepository()
        instance = CodesViewModel(
            observeLoginTotpEntries = observeLoginTotpEntries,
            totpManager = FakeTotpManager(),
            clipboardManager = FakeClipboardManager(),
            snackbarDispatcher = FakeSnackbarDispatcher(),
            userPreferencesRepository = preferenceRepository
        )
    }

    @Test
    fun `canLoadExternalImages is false when favicons are disabled`() = runTest {
        preferenceRepository.setUseFaviconsPreference(UseFaviconsPreference.Disabled)

        instance.uiState.test {
            assertThat(awaitLoadedState().canLoadExternalImages).isFalse()
        }
    }

    @Test
    fun `canLoadExternalImages is true when favicons are enabled`() = runTest {
        preferenceRepository.setUseFaviconsPreference(UseFaviconsPreference.Enabled)

        instance.uiState.test {
            val state = awaitLoadedState()
            assertThat(state.canLoadExternalImages).isTrue()
            assertThat(state.rows).hasSize(1)
        }
    }

    @Test
    fun `canLoadExternalImages follows favicons preference changes`() = runTest {
        preferenceRepository.setUseFaviconsPreference(UseFaviconsPreference.Disabled)

        instance.uiState.test {
            assertThat(awaitLoadedState().canLoadExternalImages).isFalse()

            preferenceRepository.setUseFaviconsPreference(UseFaviconsPreference.Enabled)
            assertThat(awaitItem().canLoadExternalImages).isTrue()
        }
    }

    private suspend fun ReceiveTurbine<CodesUiState>.awaitLoadedState(): CodesUiState {
        var state = awaitItem()
        while (state.isLoading) {
            state = awaitItem()
        }
        return state
    }

    private fun entry() = LoginTotpEntry(
        shareId = ShareId("share-1"),
        itemId = ItemId("item-1"),
        itemTitle = "Example",
        subtitle = "user@example.com",
        websites = listOf("https://example.com"),
        packageName = null,
        totpUri = "otpauth://totp/example?secret=ABC",
        source = LoginTotpEntry.Source.Primary
    )
}
