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

package proton.android.pass.features.security.center.excludeditems.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.fakes.usecases.FakeObserveItems
import proton.android.pass.data.fakes.usecases.vaults.FakeObserveVaultsGroupedByShareId
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemId
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.telemetry.fakes.FakeTelemetryManager
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.ItemTestFactory

internal class SecurityCenterExcludedItemsViewModelTest {

    @get:Rule
    internal val dispatcherRule = MainDispatcherRule()

    private lateinit var observeItems: FakeObserveItems
    private lateinit var observeVaultsGroupedByShareId: FakeObserveVaultsGroupedByShareId
    private lateinit var userPreferencesRepository: FakePreferenceRepository
    private lateinit var telemetryManager: FakeTelemetryManager
    private lateinit var encryptionContextProvider: FakeEncryptionContextProvider

    @Before
    internal fun setUp() {
        observeItems = FakeObserveItems()
        observeVaultsGroupedByShareId = FakeObserveVaultsGroupedByShareId()
        userPreferencesRepository = FakePreferenceRepository()
        telemetryManager = FakeTelemetryManager()
        encryptionContextProvider = FakeEncryptionContextProvider()
    }

    @Test
    internal fun `WHEN excluded items are emitted THEN items are sorted alphabetically by title`() = runTest {
        val itemC = ItemTestFactory.createLogin(
            itemId = ItemId("c"),
            title = "Charlie",
            flags = ItemFlag.SkipHealthCheck.value
        )
        val itemA = ItemTestFactory.createLogin(
            itemId = ItemId("a"),
            title = "Alpha",
            flags = ItemFlag.SkipHealthCheck.value
        )
        val itemB = ItemTestFactory.createLogin(
            itemId = ItemId("b"),
            title = "bravo",
            flags = ItemFlag.SkipHealthCheck.value
        )

        observeVaultsGroupedByShareId.emitDefault()

        val viewModel = createViewModel()

        observeItems.emitValue(listOf(itemC, itemA, itemB))

        viewModel.state.test {
            val state = awaitItem()
            val titles = state.excludedItemUiModels.map { it.contents.title }
            assertThat(titles).isEqualTo(listOf("Alpha", "bravo", "Charlie"))
        }
    }

    private fun createViewModel() = SecurityCenterExcludedItemsViewModel(
        observeItems = observeItems,
        observeVaultsGroupedByShareId = observeVaultsGroupedByShareId,
        userPreferencesRepository = userPreferencesRepository,
        encryptionContextProvider = encryptionContextProvider,
        telemetryManager = telemetryManager
    )
}
