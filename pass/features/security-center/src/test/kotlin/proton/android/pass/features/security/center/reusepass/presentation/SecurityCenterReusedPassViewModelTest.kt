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

package proton.android.pass.features.security.center.reusepass.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.fakes.usecases.items.FakeObserveMonitoredItems
import proton.android.pass.data.fakes.usecases.vaults.FakeObserveVaultsGroupedByShareId
import proton.android.pass.domain.ItemId
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.securitycenter.api.passwords.RepeatedPasswordsReport
import proton.android.pass.securitycenter.fakes.passwords.FakeRepeatedPasswordChecker
import proton.android.pass.telemetry.fakes.FakeTelemetryManager
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.ItemTestFactory

internal class SecurityCenterReusedPassViewModelTest {

    @get:Rule
    internal val dispatcherRule = MainDispatcherRule()

    private lateinit var observeMonitoredItems: FakeObserveMonitoredItems
    private lateinit var observeVaultsGroupedByShareId: FakeObserveVaultsGroupedByShareId
    private lateinit var repeatedPasswordChecker: FakeRepeatedPasswordChecker
    private lateinit var userPreferencesRepository: FakePreferenceRepository
    private lateinit var telemetryManager: FakeTelemetryManager
    private lateinit var encryptionContextProvider: FakeEncryptionContextProvider

    @Before
    internal fun setUp() {
        observeMonitoredItems = FakeObserveMonitoredItems()
        observeVaultsGroupedByShareId = FakeObserveVaultsGroupedByShareId()
        repeatedPasswordChecker = FakeRepeatedPasswordChecker()
        userPreferencesRepository = FakePreferenceRepository()
        telemetryManager = FakeTelemetryManager()
        encryptionContextProvider = FakeEncryptionContextProvider()
    }

    @Test
    internal fun `WHEN reused passwords are emitted THEN items within each group are sorted alphabetically`() =
        runTest {
            val itemC = ItemTestFactory.createLogin(
                itemId = ItemId("c"),
                title = "Charlie",
                password = "same"
            )
            val itemA = ItemTestFactory.createLogin(
                itemId = ItemId("a"),
                title = "Alpha",
                password = "same"
            )
            val itemB = ItemTestFactory.createLogin(
                itemId = ItemId("b"),
                title = "Bravo",
                password = "same"
            )

            val encryptedPassword = FakeEncryptionContext.encrypt("same")

            repeatedPasswordChecker.setResult(
                Result.success(
                    RepeatedPasswordsReport(
                        repeatedPasswords = mapOf(
                            encryptedPassword to listOf(itemC, itemA, itemB)
                        )
                    )
                )
            )

            observeVaultsGroupedByShareId.emitDefault()

            val viewModel = createViewModel()

            observeMonitoredItems.emitMonitoredItems(listOf(itemC, itemA, itemB))

            viewModel.state.test {
                skipItems(1)
                val state = awaitItem()
                val group = state.reusedPasswords.first()
                val titles = group.itemUiModels.map { it.contents.title }
                assertThat(titles).isEqualTo(listOf("Alpha", "Bravo", "Charlie"))
            }
        }

    private fun createViewModel() = SecurityCenterReusedPassViewModel(
        observeMonitoredItems = observeMonitoredItems,
        observeVaultsGroupedByShareId = observeVaultsGroupedByShareId,
        repeatedPasswordChecker = repeatedPasswordChecker,
        userPreferencesRepository = userPreferencesRepository,
        telemetryManager = telemetryManager,
        encryptionContextProvider = encryptionContextProvider
    )
}
