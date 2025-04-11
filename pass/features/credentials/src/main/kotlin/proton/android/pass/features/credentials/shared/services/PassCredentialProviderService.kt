/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.credentials.shared.services

import android.os.Build
import android.os.CancellationSignal
import android.os.OutcomeReceiver
import androidx.annotation.RequiresApi
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.ClearCredentialUnknownException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.CreateCredentialUnknownException
import androidx.credentials.exceptions.CreateCredentialUnsupportedException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialUnknownException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.provider.Action
import androidx.credentials.provider.BeginCreateCredentialRequest
import androidx.credentials.provider.BeginCreateCredentialResponse
import androidx.credentials.provider.BeginCreatePasswordCredentialRequest
import androidx.credentials.provider.BeginCreatePublicKeyCredentialRequest
import androidx.credentials.provider.BeginGetCredentialRequest
import androidx.credentials.provider.BeginGetCredentialResponse
import androidx.credentials.provider.BeginGetPasswordOption
import androidx.credentials.provider.BeginGetPublicKeyCredentialOption
import androidx.credentials.provider.CredentialEntry
import androidx.credentials.provider.CredentialProviderService
import androidx.credentials.provider.ProviderClearCredentialStateRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.features.credentials.shared.passkeys.create.PasskeyCredentialsCreator
import proton.android.pass.features.credentials.shared.passkeys.search.PasskeyCredentialsSearcher
import proton.android.pass.features.credentials.shared.passwords.create.PasswordCredentialsCreator
import proton.android.pass.features.credentials.shared.passwords.search.PasswordCredentialsSearcher
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

@[AndroidEntryPoint RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)]
class PassCredentialProviderService : CredentialProviderService() {

    @Inject
    internal lateinit var appDispatchers: AppDispatchers

    @Inject
    internal lateinit var passwordCredentialsCreator: PasswordCredentialsCreator

    @Inject
    internal lateinit var passkeyCredentialsCreator: PasskeyCredentialsCreator

    @Inject
    internal lateinit var passwordCredentialsSearcher: PasswordCredentialsSearcher

    @Inject
    internal lateinit var passkeyCredentialsSearcher: PasskeyCredentialsSearcher

    override fun onBeginCreateCredentialRequest(
        request: BeginCreateCredentialRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<BeginCreateCredentialResponse, CreateCredentialException>
    ) {
        val handler = CoroutineExceptionHandler { _, exception ->
            PassLogger.w(TAG, "Error handling creating credential request")
            PassLogger.w(TAG, exception)

            (exception as? CreateCredentialException ?: CreateCredentialUnknownException())
                .also(callback::onError)
        }

        val job = CoroutineScope(appDispatchers.io).launch(handler) {
            when (request) {
                is BeginCreatePasswordCredentialRequest -> {
                    passwordCredentialsCreator.create(applicationContext)
                }

                is BeginCreatePublicKeyCredentialRequest -> {
                    passkeyCredentialsCreator.create(applicationContext, request)
                }

                else -> {
                    throw CreateCredentialUnsupportedException()
                }
            }
                .let(::BeginCreateCredentialResponse)
                .also(callback::onResult)
        }

        cancellationSignal.setOnCancelListener {
            PassLogger.w(TAG, "CancellationSignal received on create credential request")

            job.cancel()
        }
    }

    override fun onBeginGetCredentialRequest(
        request: BeginGetCredentialRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<BeginGetCredentialResponse, GetCredentialException>
    ) {
        val handler = CoroutineExceptionHandler { _, exception ->
            PassLogger.w(TAG, "Error handling getting credential request")
            PassLogger.w(TAG, exception)

            (exception as? GetCredentialException ?: GetCredentialUnknownException())
                .also(callback::onError)
        }

        val job = CoroutineScope(appDispatchers.io).launch(handler) {
            val credentialEntries = mutableListOf<CredentialEntry>()
            val actions = mutableListOf<Action>()

            for (option in request.beginGetCredentialOptions) {
                when (option) {
                    is BeginGetPasswordOption -> {
                        passwordCredentialsSearcher.search(applicationContext, option)
                    }

                    is BeginGetPublicKeyCredentialOption -> {
                        passkeyCredentialsSearcher.search(applicationContext, option)
                    }

                    else -> {
                        throw GetCredentialUnsupportedException()
                    }
                }?.also { (newCredentialEntries, newAction) ->
                    credentialEntries.addAll(newCredentialEntries)
                    actions.add(newAction)
                }
            }

            BeginGetCredentialResponse(
                credentialEntries = credentialEntries,
                actions = actions
            ).also(callback::onResult)
        }

        cancellationSignal.setOnCancelListener {
            PassLogger.w(TAG, "CancellationSignal received on get credential request")

            job.cancel()
        }
    }

    override fun onClearCredentialStateRequest(
        request: ProviderClearCredentialStateRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<Void?, ClearCredentialException>
    ) {
        PassLogger.i(TAG, "onClearCredentialStateRequest invoked")

        callback.onError(ClearCredentialUnknownException())
    }

    private companion object {

        private const val TAG = "PassCredentialProviderService"

    }

}
