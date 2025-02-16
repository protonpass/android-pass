/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.passkeys.service

import android.os.Build
import android.os.CancellationSignal
import android.os.OutcomeReceiver
import androidx.annotation.RequiresApi
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.ClearCredentialUnknownException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.provider.BeginCreateCredentialRequest
import androidx.credentials.provider.BeginCreateCredentialResponse
import androidx.credentials.provider.BeginGetCredentialRequest
import androidx.credentials.provider.BeginGetCredentialResponse
import androidx.credentials.provider.CredentialProviderService
import androidx.credentials.provider.ProviderClearCredentialStateRequest
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.biometry.NeedsBiometricAuth
import proton.android.pass.data.api.usecases.passkeys.GetPasskeysForDomain
import proton.android.pass.log.api.PassLogger
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class PasskeyProviderService : CredentialProviderService() {

    @Inject
    lateinit var getPasskeysForDomain: GetPasskeysForDomain

    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var needsBiometricAuth: NeedsBiometricAuth

    @Inject
    lateinit var telemetryManager: TelemetryManager

    override fun onBeginCreateCredentialRequest(
        request: BeginCreateCredentialRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<BeginCreateCredentialResponse, CreateCredentialException>
    ) {
        PassLogger.i(TAG, "Received onBeginCreateCredentialRequest")
        proton.android.pass.features.passkeys.service.CreatePasskeyHandler.handle(
            context = applicationContext,
            request = request,
            cancellationSignal = cancellationSignal,
            callback = callback,
            accountManager = accountManager,
            telemetryManager = telemetryManager
        )
    }

    override fun onBeginGetCredentialRequest(
        request: BeginGetCredentialRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<BeginGetCredentialResponse, GetCredentialException>
    ) {
        PassLogger.i(TAG, "Received onBeginGetCredentialRequest")
        GetPasskeysHandler.handle(
            context = applicationContext,
            request = request,
            cancellationSignal = cancellationSignal,
            callback = callback,
            getPasskeysForDomain = getPasskeysForDomain,
            accountManager = accountManager,
            needsBiometricAuth = needsBiometricAuth,
            telemetryManager = telemetryManager
        )
    }

    override fun onClearCredentialStateRequest(
        request: ProviderClearCredentialStateRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<Void?, ClearCredentialException>
    ) {
        PassLogger.i(TAG, "Received onClearCredentialStateRequest")
        callback.onError(ClearCredentialUnknownException())
    }

    companion object {
        private const val TAG = "PasskeyProviderService"
    }
}
