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
import me.proton.core.auth.fido.domain.entity.Fido2RegisteredKey
import me.proton.core.usersettings.domain.entity.TwoFASetting
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.usecase.ObserveRegisteredSecurityKeys
import me.proton.core.usersettings.domain.usecase.ObserveUserSettings
import org.junit.Before
import org.junit.Test
import proton.android.pass.account.fakes.FakeAccountRepository
import proton.android.pass.account.fakes.FakeIsFido2Enabled
import proton.android.pass.account.fakes.FakeUserSettingsRepository
import proton.android.pass.domain.UserId

class ObserveSecurityKeysImplTest {

    private lateinit var isFido2Enabled: FakeIsFido2Enabled
    private lateinit var userSettingsRepository: FakeUserSettingsRepository
    private lateinit var instance: ObserveSecurityKeysImpl

    @Before
    fun setup() {
        isFido2Enabled = FakeIsFido2Enabled()
        userSettingsRepository = FakeUserSettingsRepository()
        instance = ObserveSecurityKeysImpl(
            observeRegisteredSecurityKeys = ObserveRegisteredSecurityKeys(
                accountRepository = FakeAccountRepository(),
                isFido2Enabled = isFido2Enabled,
                observeUserSettings = ObserveUserSettings(userSettingsRepository)
            )
        )
    }

    @Test
    fun `emits no keys when fido2 is disabled`() = runTest {
        isFido2Enabled.enabled = false
        userSettingsRepository.setUserSettings(userSettings("key one"))

        instance(USER_ID).test {
            assertThat(awaitItem()).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `emits the registered key names when fido2 is enabled`() = runTest {
        isFido2Enabled.enabled = true
        userSettingsRepository.setUserSettings(userSettings("key one", "key two"))

        instance(USER_ID).test {
            assertThat(awaitItem().map { it.name }).containsExactly("key one", "key two").inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits no keys when fido2 is enabled and none are registered`() = runTest {
        isFido2Enabled.enabled = true
        userSettingsRepository.setUserSettings(userSettings())

        instance(USER_ID).test {
            assertThat(awaitItem()).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun userSettings(vararg keyNames: String): UserSettings = UserSettings.nil(USER_ID).copy(
        twoFA = TwoFASetting(
            enabled = null,
            allowed = null,
            expirationTime = null,
            registeredKeys = keyNames.map {
                Fido2RegisteredKey(attestationFormat = "none", credentialID = UByteArray(0), name = it)
            }
        )
    )

    private companion object {
        val USER_ID = UserId("12345")
    }
}
