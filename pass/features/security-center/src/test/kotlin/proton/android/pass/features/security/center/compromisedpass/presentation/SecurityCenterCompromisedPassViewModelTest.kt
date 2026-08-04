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

package proton.android.pass.features.security.center.compromisedpass.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.proton.core.test.kotlin.TestDispatcherProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.api.repositories.CompromisedPasswordItem
import proton.android.pass.data.fakes.usecases.compromisedpassword.FakeObserveCompromisedPasswords
import proton.android.pass.data.fakes.usecases.items.FakeObserveMonitoredItems
import proton.android.pass.data.fakes.usecases.vaults.FakeObserveVaultsGroupedByShareId
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemId
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.telemetry.fakes.FakeTelemetryManager
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.ItemTestFactory

internal class SecurityCenterCompromisedPassViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    internal val dispatcherRule = MainDispatcherRule(testDispatcher)

    private lateinit var observeMonitoredItems: FakeObserveMonitoredItems
    private lateinit var observeVaultsGroupedByShareId: FakeObserveVaultsGroupedByShareId
    private lateinit var observeCompromisedPasswords: FakeObserveCompromisedPasswords
    private lateinit var userPreferencesRepository: FakePreferenceRepository
    private lateinit var telemetryManager: FakeTelemetryManager
    private lateinit var encryptionContextProvider: FakeEncryptionContextProvider

    @Before
    internal fun setUp() {
        observeMonitoredItems = FakeObserveMonitoredItems()
        observeVaultsGroupedByShareId = FakeObserveVaultsGroupedByShareId()
        observeCompromisedPasswords = FakeObserveCompromisedPasswords()
        userPreferencesRepository = FakePreferenceRepository()
        telemetryManager = FakeTelemetryManager()
        encryptionContextProvider = FakeEncryptionContextProvider()
    }

    @Test
    internal fun `WHEN compromised passwords are emitted THEN they are listed`() = runTest {
        val compromised = ItemTestFactory.createLogin(itemId = ItemId("a"), title = "Alpha")
        val safe = ItemTestFactory.createLogin(itemId = ItemId("b"), title = "Bravo")

        observeVaultsGroupedByShareId.emitDefault()
        observeCompromisedPasswords.emit(
            listOf(CompromisedPasswordItem(compromised.shareId, compromised.id))
        )

        val viewModel = createViewModel()

        observeMonitoredItems.emitMonitoredItems(listOf(compromised, safe))

        viewModel.state.filterNot { it.isLoading }.test {
            val titles = awaitItem().compromisedPasswordUiModels.map { it.contents.title }
            assertThat(titles).isEqualTo(listOf("Alpha"))
        }
    }

    @Test
    internal fun `WHEN an item skips the compromised check THEN it is not listed`() = runTest {
        val ignored = ItemTestFactory.createLogin(
            itemId = ItemId("a"),
            title = "Alpha",
            flags = ItemFlag.SkipCompromisedPasswordCheck.value
        )
        val compromised = ItemTestFactory.createLogin(itemId = ItemId("b"), title = "Bravo")

        observeVaultsGroupedByShareId.emitDefault()
        observeCompromisedPasswords.emit(
            listOf(
                CompromisedPasswordItem(ignored.shareId, ignored.id),
                CompromisedPasswordItem(compromised.shareId, compromised.id)
            )
        )

        val viewModel = createViewModel()

        observeMonitoredItems.emitMonitoredItems(listOf(ignored, compromised))

        viewModel.state.filterNot { it.isLoading }.test {
            val titles = awaitItem().compromisedPasswordUiModels.map { it.contents.title }
            assertThat(titles).isEqualTo(listOf("Bravo"))
        }
    }

    private fun createViewModel() = SecurityCenterCompromisedPassViewModel(
        observeMonitoredItems = observeMonitoredItems,
        observeVaultsGroupedByShareId = observeVaultsGroupedByShareId,
        observeCompromisedPasswords = observeCompromisedPasswords,
        userPreferencesRepository = userPreferencesRepository,
        telemetryManager = telemetryManager,
        encryptionContextProvider = encryptionContextProvider,
        dispatcherProvider = TestDispatcherProvider(testDispatcher)
    )
}
