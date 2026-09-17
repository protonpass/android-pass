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

package proton.android.pass.searchoptions.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.fakes.usecases.FakeObserveCurrentUser
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.searchoptions.api.VaultSelectionOption
import proton.android.pass.test.domain.UserTestFactory

class HomeSearchOptionsRepositoryImplTest {

    private lateinit var instance: HomeSearchOptionsRepositoryImpl

    private lateinit var observeCurrentUser: FakeObserveCurrentUser
    private lateinit var internalSettingsRepository: FakeInternalSettingsRepository

    @Before
    fun setup() {
        observeCurrentUser = FakeObserveCurrentUser()
        internalSettingsRepository = FakeInternalSettingsRepository()

        instance = HomeSearchOptionsRepositoryImpl(
            observeCurrentUser = observeCurrentUser,
            internalSettingsRepository = internalSettingsRepository
        )

        observeCurrentUser.sendUser(UserTestFactory.create())
    }

    @Test
    fun `isInSeeAllPinsMode can be set and observed`() = runTest {
        instance.observeIsInSeeAllPinsMode().test {
            val initial = awaitItem()
            assertThat(initial).isFalse()

            instance.setIsInSeeAllPinsMode(true)
            val afterSet = awaitItem()
            assertThat(afterSet).isTrue()

            instance.setIsInSeeAllPinsMode(false)
            val afterReset = awaitItem()
            assertThat(afterReset).isFalse()
        }
    }

    @Test
    fun `setVaultSelectionOption resets isInSeeAllPinsMode to false`() = runTest {
        instance.observeIsInSeeAllPinsMode().test {
            val initial = awaitItem()
            assertThat(initial).isFalse()

            instance.setIsInSeeAllPinsMode(true)
            val afterSet = awaitItem()
            assertThat(afterSet).isTrue()

            instance.setVaultSelectionOption(VaultSelectionOption.AllVaults)
            val afterVaultChange = awaitItem()
            assertThat(afterVaultChange).isFalse()
        }
    }

    @Test
    fun `setVaultSelectionOption resets pins mode when vault changes from see all pinned`() = runTest {
        instance.setIsInSeeAllPinsMode(true)

        instance.observeIsInSeeAllPinsMode().test {
            val initial = awaitItem()
            assertThat(initial).isTrue()

            instance.setVaultSelectionOption(VaultSelectionOption.Trash)
            val afterVaultChange = awaitItem()
            assertThat(afterVaultChange).isFalse()
        }
    }
}
