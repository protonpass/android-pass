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

package proton.android.pass.data.impl.usecases

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.usersettings.domain.entity.RecoverySetting
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.usecase.ObserveUserSettings
import org.junit.Before
import org.junit.Test
import proton.android.pass.account.fakes.FakeUserSettingsRepository
import proton.android.pass.data.fakes.usecases.FakeObserveCurrentUser
import proton.android.pass.domain.UserId
import proton.android.pass.test.domain.UserTestFactory

class ObserveCurrentUserSettingsImplTest {

    private lateinit var observeCurrentUser: FakeObserveCurrentUser
    private lateinit var userSettingsRepository: FakeUserSettingsRepository
    private lateinit var instance: ObserveCurrentUserSettingsImpl

    @Before
    fun setup() {
        observeCurrentUser = FakeObserveCurrentUser()
        userSettingsRepository = FakeUserSettingsRepository()
        instance = ObserveCurrentUserSettingsImpl(
            observeCurrentUser = observeCurrentUser,
            observeUserSettings = ObserveUserSettings(userSettingsRepository)
        )
    }

    @Test
    fun `emits the recovery email of the current user settings`() = runTest {
        userSettingsRepository.setUserSettings(userSettings(recoveryEmail = "recovery@email.local"))

        instance().test {
            observeCurrentUser.sendUser(UserTestFactory.create(userId = USER_ID))

            assertThat(awaitItem()?.recoveryEmail).isEqualTo("recovery@email.local")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits a null recovery email when the settings have none`() = runTest {
        userSettingsRepository.setUserSettings(userSettings(recoveryEmail = null))

        instance().test {
            observeCurrentUser.sendUser(UserTestFactory.create(userId = USER_ID))

            assertThat(awaitItem()?.recoveryEmail).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun userSettings(recoveryEmail: String?): UserSettings = UserSettings.nil(USER_ID).copy(
        email = recoveryEmail?.let { RecoverySetting(value = it, status = null, notify = null, reset = null) }
    )

    private companion object {
        val USER_ID = UserId("12345")
    }
}
