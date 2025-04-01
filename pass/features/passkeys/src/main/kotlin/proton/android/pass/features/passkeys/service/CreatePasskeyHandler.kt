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

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CancellationSignal
import android.os.OutcomeReceiver
import androidx.annotation.RequiresApi
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.CreateCredentialUnknownException
import androidx.credentials.provider.BeginCreateCredentialRequest
import androidx.credentials.provider.BeginCreateCredentialResponse
import androidx.credentials.provider.BeginCreatePublicKeyCredentialRequest
import androidx.credentials.provider.CreateEntry
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import proton.android.pass.features.passkeys.create.ui.CreatePasskeyActivity
import proton.android.pass.features.passkeys.telemetry.CreatePromptDisplay
import proton.android.pass.log.api.PassLogger
import proton.android.pass.telemetry.api.TelemetryManager

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
object CreatePasskeyHandler {

    private const val TAG = "CreatePasskeyHandler"

    @Suppress("LongParameterList")
    fun handle(
        context: Context,
        request: BeginCreateCredentialRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<BeginCreateCredentialResponse, CreateCredentialException>,
        accountManager: AccountManager,
        telemetryManager: TelemetryManager
    ) {
        val handler = CoroutineExceptionHandler { _, exception ->
            PassLogger.w(TAG, "Error handling create passkey")
            PassLogger.w(TAG, exception)
            callback.onError(CreateCredentialUnknownException("There was an error"))
        }

        val job = CoroutineScope(Dispatchers.IO).launch(handler) {
            val response =
                processCreateCredentialRequest(
                    context = context,
                    request = request,
                    accountManager = accountManager,
                    telemetryManager = telemetryManager
                )
            callback.onResult(response)
        }

        cancellationSignal.setOnCancelListener {
            PassLogger.w(TAG, "cancellationSignal received")
            job.cancel()
        }
    }

    private suspend fun processCreateCredentialRequest(
        context: Context,
        request: BeginCreateCredentialRequest,
        accountManager: AccountManager,
        telemetryManager: TelemetryManager
    ): BeginCreateCredentialResponse? {
        val currentUser = accountManager.getPrimaryAccount().first()
        if (currentUser == null) {
            PassLogger.d(TAG, "No user found")
            return null
        }

        return when (request) {
            is BeginCreatePublicKeyCredentialRequest -> handleCreatePasskeyQuery(
                context = context,
                username = currentUser.username,
                telemetryManager = telemetryManager
            )
            else -> null
        }
    }

    private fun handleCreatePasskeyQuery(
        context: Context,
        username: String?,
        telemetryManager: TelemetryManager
    ): BeginCreateCredentialResponse {
        val createEntries = listOf(
            CreateEntry(
                accountName = username.orEmpty(),
                pendingIntent = createNewPendingIntent(context)
            )
        )

        telemetryManager.sendEvent(CreatePromptDisplay)
        return BeginCreateCredentialResponse(createEntries)
    }

    private fun createNewPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, CreatePasskeyActivity::class.java)
            .setPackage(context.packageName)

        return PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

}
