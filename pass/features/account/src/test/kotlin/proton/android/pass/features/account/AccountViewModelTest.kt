/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.features.account

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.fakes.usecases.FakeIsSecurityKeysEnabled
import proton.android.pass.data.fakes.usecases.FakeObserveAccountStateChanges
import proton.android.pass.data.fakes.usecases.FakeObserveCurrentUser
import proton.android.pass.data.fakes.usecases.FakeObserveCurrentUserSettings
import proton.android.pass.data.fakes.usecases.FakeObserveSecurityKeys
import proton.android.pass.data.fakes.usecases.FakeObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.accesskey.FakeHasExtraPassword
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.account.AccountState
import proton.android.pass.domain.account.SecurityKey
import proton.android.pass.domain.account.UserSettings
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.AccountTestFactory
import proton.android.pass.test.domain.UserTestFactory

internal class AccountViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: AccountViewModel
    private lateinit var observeCurrentUser: FakeObserveCurrentUser
    private lateinit var observeCurrentUserSettings: FakeObserveCurrentUserSettings
    private lateinit var getUpgradeInfo: FakeObserveUpgradeInfo
    private lateinit var snackbarDispatcher: FakeSnackbarDispatcher
    private lateinit var observeAccountStateChanges: FakeObserveAccountStateChanges

    private lateinit var isSecurityKeysEnabled: FakeIsSecurityKeysEnabled
    private lateinit var observeSecurityKeys: FakeObserveSecurityKeys

    @Before
    fun setup() {
        observeCurrentUser = FakeObserveCurrentUser()
        observeCurrentUserSettings = FakeObserveCurrentUserSettings()
        getUpgradeInfo = FakeObserveUpgradeInfo()
        snackbarDispatcher = FakeSnackbarDispatcher()
        observeAccountStateChanges = FakeObserveAccountStateChanges()
        isSecurityKeysEnabled = FakeIsSecurityKeysEnabled()
        observeSecurityKeys = FakeObserveSecurityKeys()

        instance = AccountViewModel(
            observeCurrentUser = observeCurrentUser,
            observeUpgradeInfo = getUpgradeInfo,
            observeCurrentUserSettings = observeCurrentUserSettings,
            hasExtraPassword = FakeHasExtraPassword(),
            observeAccountStateChanges = observeAccountStateChanges,
            isSecurityKeysEnabled = isSecurityKeysEnabled,
            observeSecurityKeys = observeSecurityKeys
        )
    }

    @Test
    fun `emits initial state`() = runTest {
        instance.state.test {
            assertThat(awaitItem()).isEqualTo(AccountUiState.Initial)
        }
    }

    @Test
    fun `emits user email and plan`() = runTest {
        val email = "test@email.local"
        val planType = PlanType.Paid.Plus(
            name = "internal",
            displayName = "testplan"
        )
        val plan = Plan(
            planType = planType,
            vaultLimit = PlanLimit.Unlimited,
            aliasLimit = PlanLimit.Unlimited,
            totpLimit = PlanLimit.Unlimited,
            updatedAt = 0,
            hideUpgrade = false
        )
        val user = UserTestFactory.create(email = email)
        observeCurrentUser.sendUser(user)
        getUpgradeInfo.setResult(
            UpgradeInfo(
                plan = plan,
                isUpgradeAvailable = false,
                isSubscriptionAvailable = false,
                totalVaults = 0,
                totalAlias = 0,
                totalTotp = 0
            )
        )

        instance.state.test {
            val item = awaitItem()
            assertThat(item.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.email).isEqualTo(email)
            assertThat(item.plan).isEqualTo(PlanSection.Data(planType.humanReadableName))
        }
    }

    @Test
    fun `hasBeenSignedOut is false initially`() = runTest {
        instance.hasBeenSignedOut.test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `hasBeenSignedOut stays false when an account becomes ready`() = runTest {
        instance.hasBeenSignedOut.test {
            assertThat(awaitItem()).isFalse()

            observeAccountStateChanges.emitChange(accountWithState(AccountState.Ready))

            expectNoEvents()
        }
    }

    @Test
    fun `hasBeenSignedOut stays false for other account states`() = runTest {
        instance.hasBeenSignedOut.test {
            assertThat(awaitItem()).isFalse()

            observeAccountStateChanges.emitChange(accountWithState(AccountState.Other))

            expectNoEvents()
        }
    }

    @Test
    fun `hasBeenSignedOut becomes true when an account is disabled`() = runTest {
        instance.hasBeenSignedOut.test {
            assertThat(awaitItem()).isFalse()

            observeAccountStateChanges.emitChange(accountWithState(AccountState.Disabled))

            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `hasBeenSignedOut becomes true when an account is removed`() = runTest {
        instance.hasBeenSignedOut.test {
            assertThat(awaitItem()).isFalse()

            observeAccountStateChanges.emitChange(accountWithState(AccountState.Removed))

            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `recovery email is null when no user settings are emitted`() = runTest {
        observeCurrentUser.sendUser(UserTestFactory.create(email = EMAIL))

        instance.state.test {
            val item = awaitMatching { it.userId != null }
            assertThat(item.recoveryEmail).isNull()
        }
    }

    @Test
    fun `recovery email is taken from the current user settings`() = runTest {
        observeCurrentUser.sendUser(UserTestFactory.create(email = EMAIL))
        observeCurrentUserSettings.emit(UserSettings(recoveryEmail = RECOVERY_EMAIL))

        instance.state.test {
            val item = awaitMatching { it.recoveryEmail != null }
            assertThat(item.recoveryEmail).isEqualTo(RECOVERY_EMAIL)
        }
    }

    @Test
    fun `recovery email is null when the user settings have no recovery email`() = runTest {
        observeCurrentUser.sendUser(UserTestFactory.create(email = EMAIL))
        observeCurrentUserSettings.emit(UserSettings(recoveryEmail = null))

        instance.state.test {
            val item = awaitMatching { it.userId != null }
            assertThat(item.recoveryEmail).isNull()
        }
    }

    @Test
    fun `security keys section is hidden when security keys are disabled`() = runTest {
        isSecurityKeysEnabled.enabled = false
        observeCurrentUser.sendUser(UserTestFactory.create(email = EMAIL))

        instance.state.test {
            val item = awaitMatching { it.userId != null }
            assertThat(item.showSecurityKeys).isFalse()
        }
    }

    @Test
    fun `security keys are shown with their names when security keys are enabled`() = runTest {
        isSecurityKeysEnabled.enabled = true
        observeSecurityKeys.setKeys(securityKeys("key one", "key two"))
        observeCurrentUser.sendUser(UserTestFactory.create(email = EMAIL))

        instance.state.test {
            val item = awaitMatching { it.userId != null }
            assertThat(item.showSecurityKeys).isTrue()
            assertThat(item.registeredSecurityKeys?.map { it.name }).containsExactly("key one", "key two").inOrder()
        }
    }

    @Test
    fun `security keys section is shown empty when enabled and none are registered`() = runTest {
        isSecurityKeysEnabled.enabled = true
        observeSecurityKeys.setKeys(emptyList())
        observeCurrentUser.sendUser(UserTestFactory.create(email = EMAIL))

        instance.state.test {
            val item = awaitMatching { it.userId != null }
            assertThat(item.showSecurityKeys).isTrue()
            assertThat(item.registeredSecurityKeys).isEmpty()
        }
    }

    @Test
    fun `sso users do not see security keys, change password or recovery email`() = runTest {
        isSecurityKeysEnabled.enabled = true
        observeSecurityKeys.setKeys(securityKeys("key one"))
        observeCurrentUser.sendUser(UserTestFactory.create(email = EMAIL, isSso = true))

        instance.state.test {
            val item = awaitMatching { it.userId != null }
            assertThat(item.showSecurityKeys).isFalse()
            assertThat(item.showChangePassword).isFalse()
            assertThat(item.showRecoveryEmail).isFalse()
        }
    }

    private suspend fun ReceiveTurbine<AccountUiState>.awaitMatching(
        predicate: (AccountUiState) -> Boolean
    ): AccountUiState {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }

    private fun securityKeys(vararg names: String): List<SecurityKey> = names.map { SecurityKey(name = it) }

    private fun accountWithState(state: AccountState) =
        AccountTestFactory.create(userId = AccountTestFactory.DEFAULT_ACCOUNT.userId, state = state)

    private companion object {
        const val EMAIL = "test@email.local"
        const val RECOVERY_EMAIL = "recovery@email.local"
    }
}
