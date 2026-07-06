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

package proton.android.pass.features.credentials.passkeys.usage.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.Some
import proton.android.pass.data.fakes.usecases.FakeGetPasskeyById
import proton.android.pass.domain.ByteArrayWrapper
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.PasskeyId
import proton.android.pass.domain.ShareId
import proton.android.pass.passkeys.api.PasskeyAuthenticationResponse
import proton.android.pass.passkeys.fakes.FakeAuthenticateWithPasskey
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.telemetry.fakes.FakeTelemetryManager
import proton.android.pass.test.MainDispatcherRule

internal class PasskeyCredentialUsageViewModelTest {

    @get:Rule
    internal val dispatcherRule = MainDispatcherRule()

    private lateinit var authenticateWithPasskey: FakeAuthenticateWithPasskey
    private lateinit var getPasskeyById: FakeGetPasskeyById
    private lateinit var preferenceRepository: FakePreferenceRepository
    private lateinit var telemetryManager: FakeTelemetryManager
    private lateinit var viewModel: PasskeyCredentialUsageViewModel

    @Before
    internal fun setUp() {
        authenticateWithPasskey = FakeAuthenticateWithPasskey()
        getPasskeyById = FakeGetPasskeyById()
        preferenceRepository = FakePreferenceRepository()
        telemetryManager = FakeTelemetryManager()

        viewModel = PasskeyCredentialUsageViewModel(
            authenticateWithPasskey = authenticateWithPasskey,
            getPasskeyById = getPasskeyById,
            preferenceRepository = preferenceRepository,
            telemetryManager = telemetryManager
        )
    }

    @Test
    internal fun `WHEN the request has not arrived yet THEN state stays NotReady instead of cancelling`() = runTest {
        viewModel.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(PasskeyCredentialUsageState.NotReady)

            // Simulates the async gap between activity creation and onUpdateRequest being called,
            // caused by the origin resolver performing a live network fetch of assetlinks.json.
            // The state must not flip to Cancel while the request is simply not resolved yet.
            expectNoEvents()
        }
    }

    @Test
    internal fun `WHEN the request arrives after the async gap THEN state becomes Ready`() = runTest {
        val passkey = createPasskey()
        getPasskeyById.setResult(Some(passkey))
        authenticateWithPasskey.setResponse(Result.success(PasskeyAuthenticationResponse("auth-response")))

        viewModel.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(PasskeyCredentialUsageState.NotReady)

            viewModel.onUpdateRequest(createRequest(passkey))

            assertThat(awaitItem()).isEqualTo(PasskeyCredentialUsageState.Ready(authResponseJson = "auth-response"))
        }
    }

    @Test
    internal fun `WHEN the resolved request is genuinely null THEN state becomes Cancel`() = runTest {
        viewModel.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(PasskeyCredentialUsageState.NotReady)

            viewModel.onUpdateRequest(null)

            assertThat(awaitItem()).isEqualTo(PasskeyCredentialUsageState.Cancel)
        }
    }

    private fun createPasskey(): Passkey = Passkey(
        id = PasskeyId("passkey-id"),
        domain = "example.com",
        rpId = "example.com",
        rpName = "Example",
        userName = "user",
        userDisplayName = "User Display",
        userId = ByteArrayWrapper(ByteArray(0)),
        note = "",
        createTime = Instant.fromEpochSeconds(0),
        contents = ByteArrayWrapper(ByteArray(0)),
        userHandle = null,
        credentialId = ByteArrayWrapper(ByteArray(0)),
        creationData = null
    )

    private fun createRequest(passkey: Passkey): PasskeyCredentialUsageRequest = PasskeyCredentialUsageRequest(
        requestJson = "{}",
        requestOrigin = "https://example.com",
        clientDataHash = null,
        shareId = ShareId("share-id"),
        itemId = ItemId("item-id"),
        passkeyId = passkey.id
    )

}
