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

package proton.android.pass.features.credentials.passkeys.selection.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.appconfig.fakes.FakeAppConfig
import proton.android.pass.biometry.FakeNeedsBiometricAuth
import proton.android.pass.data.fakes.usecases.FakeGetPasskeyById
import proton.android.pass.data.fakes.usecases.FakeHasActiveAccount
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.PasskeyId
import proton.android.pass.domain.ShareId
import proton.android.pass.notifications.fakes.FakeToastManager
import proton.android.pass.passkeys.fakes.FakeAuthenticateWithPasskey
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.telemetry.fakes.FakeTelemetryManager
import proton.android.pass.test.MainDispatcherRule

internal class PasskeyCredentialSelectionViewModelTest {

    @get:Rule
    internal val dispatcherRule = MainDispatcherRule()

    private lateinit var getPasskeyById: FakeGetPasskeyById
    private lateinit var authenticateWithPasskey: FakeAuthenticateWithPasskey
    private lateinit var viewModel: PasskeyCredentialSelectionViewModel

    @Before
    internal fun setUp() {
        getPasskeyById = FakeGetPasskeyById()
        authenticateWithPasskey = FakeAuthenticateWithPasskey()

        viewModel = PasskeyCredentialSelectionViewModel(
            userPreferenceRepository = FakePreferenceRepository(),
            needsBiometricAuth = FakeNeedsBiometricAuth(),
            appConfig = FakeAppConfig(),
            accountManager = FakeAccountManager(),
            toastManager = FakeToastManager(),
            internalSettingsRepository = FakeInternalSettingsRepository(),
            authenticateWithPasskey = authenticateWithPasskey,
            getPasskeyById = getPasskeyById,
            hasActiveAccount = FakeHasActiveAccount(),
            telemetryManager = FakeTelemetryManager()
        )
    }

    @Test
    internal fun `WHEN no active account for userId THEN onAuthPerformed denies request`() = runTest {
        val hasActiveAccount = FakeHasActiveAccount()
        hasActiveAccount.setResult(false)

        val viewModelWithoutAccount = PasskeyCredentialSelectionViewModel(
            userPreferenceRepository = FakePreferenceRepository(),
            needsBiometricAuth = FakeNeedsBiometricAuth(),
            appConfig = FakeAppConfig(),
            accountManager = FakeAccountManager(),
            toastManager = FakeToastManager(),
            internalSettingsRepository = FakeInternalSettingsRepository(),
            authenticateWithPasskey = authenticateWithPasskey,
            getPasskeyById = getPasskeyById,
            hasActiveAccount = hasActiveAccount,
            telemetryManager = FakeTelemetryManager()
        )

        viewModelWithoutAccount.onUpdateRequest(
            PasskeyCredentialSelectionRequest.Use(
                requestJson = "{}",
                requestOrigin = "https://example.com",
                clientDataHash = null,
                userId = UserId("user-id"),
                shareId = ShareId("share-id"),
                itemId = ItemId("item-id"),
                passkeyId = PasskeyId("passkey-id")
            )
        )

        viewModelWithoutAccount.stateFlow.test {
            skipItems(1)

            viewModelWithoutAccount.onAuthPerformed(
                PasskeyCredentialSelectionRequest.Use(
                    requestJson = "{}",
                    requestOrigin = "https://example.com",
                    clientDataHash = null,
                    userId = UserId("user-id"),
                    shareId = ShareId("share-id"),
                    itemId = ItemId("item-id"),
                    passkeyId = PasskeyId("passkey-id")
                )
            )

            val state = awaitItem() as PasskeyCredentialSelectionState.Ready
            assertThat(state.event).isEqualTo(PasskeyCredentialSelectionStateEvent.Cancel)
        }
    }

}
