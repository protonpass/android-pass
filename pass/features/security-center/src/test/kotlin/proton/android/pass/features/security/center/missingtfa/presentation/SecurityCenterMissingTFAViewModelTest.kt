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

package proton.android.pass.features.security.center.missingtfa.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.fakes.usecases.items.FakeObserveMonitoredItems
import proton.android.pass.data.fakes.usecases.vaults.FakeObserveVaultsGroupedByShareId
import proton.android.pass.domain.ItemId
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.securitycenter.api.passwords.Missing2faReport
import proton.android.pass.securitycenter.fakes.passwords.FakeMissing2faChecker
import proton.android.pass.telemetry.fakes.FakeTelemetryManager
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.ItemTestFactory

internal class SecurityCenterMissingTFAViewModelTest {

    @get:Rule
    internal val dispatcherRule = MainDispatcherRule()

    private lateinit var observeMonitoredItems: FakeObserveMonitoredItems
    private lateinit var observeVaultsGroupedByShareId: FakeObserveVaultsGroupedByShareId
    private lateinit var missingTfaChecker: FakeMissing2faChecker
    private lateinit var userPreferencesRepository: FakePreferenceRepository
    private lateinit var telemetryManager: FakeTelemetryManager
    private lateinit var encryptionContextProvider: FakeEncryptionContextProvider

    @Before
    internal fun setUp() {
        observeMonitoredItems = FakeObserveMonitoredItems()
        observeVaultsGroupedByShareId = FakeObserveVaultsGroupedByShareId()
        missingTfaChecker = FakeMissing2faChecker()
        userPreferencesRepository = FakePreferenceRepository()
        telemetryManager = FakeTelemetryManager()
        encryptionContextProvider = FakeEncryptionContextProvider()
    }

    @Test
    internal fun `WHEN missing 2fa items are emitted THEN items are sorted alphabetically by title`() = runTest {
        val itemC = ItemTestFactory.createLogin(itemId = ItemId("c"), title = "Charlie")
        val itemA = ItemTestFactory.createLogin(itemId = ItemId("a"), title = "Alpha")
        val itemB = ItemTestFactory.createLogin(itemId = ItemId("b"), title = "bravo")

        missingTfaChecker.setResult(
            Missing2faReport(items = listOf(itemC, itemA, itemB))
        )

        observeVaultsGroupedByShareId.emitDefault()

        val viewModel = createViewModel()

        observeMonitoredItems.emitMonitoredItems(listOf(itemC, itemA, itemB))

        viewModel.state.test {
            val state = awaitItem()
            val titles = state.missingTfaItems.map { it.contents.title }
            assertThat(titles).isEqualTo(listOf("Alpha", "bravo", "Charlie"))
        }
    }

    private fun createViewModel() = SecurityCenterMissingTFAViewModel(
        observeMonitoredItems = observeMonitoredItems,
        observeVaultsGroupedByShareId = observeVaultsGroupedByShareId,
        missingTfaChecker = missingTfaChecker,
        userPreferencesRepository = userPreferencesRepository,
        telemetryManager = telemetryManager,
        encryptionContextProvider = encryptionContextProvider
    )
}
