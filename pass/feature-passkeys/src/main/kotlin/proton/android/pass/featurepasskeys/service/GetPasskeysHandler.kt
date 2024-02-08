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

package proton.android.pass.featurepasskeys.service

import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.os.CancellationSignal
import android.os.OutcomeReceiver
import androidx.annotation.RequiresApi
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialUnknownException
import androidx.credentials.provider.BeginGetCredentialRequest
import androidx.credentials.provider.BeginGetCredentialResponse
import androidx.credentials.provider.BeginGetPublicKeyCredentialOption
import androidx.credentials.provider.CredentialEntry
import androidx.credentials.provider.PublicKeyCredentialEntry
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.data.api.usecases.passkeys.GetPasskeysForDomain
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.PasskeyId
import proton.android.pass.featurepasskeys.select.ui.SelectPasskeyActivity
import proton.android.pass.log.api.PassLogger

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
object GetPasskeysHandler {

    private const val TAG = "GetPasskeysHandler"

    @Suppress("LongParameterList")
    fun handle(
        context: Context,
        request: BeginGetCredentialRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<BeginGetCredentialResponse, GetCredentialException>,
        getPasskeysForDomain: GetPasskeysForDomain,
        accountManager: AccountManager
    ) {
        val handler = CoroutineExceptionHandler { _, exception ->
            PassLogger.e(TAG, exception)
            callback.onError(GetCredentialUnknownException("There was an error"))
        }

        val job = CoroutineScope(Dispatchers.IO).launch(handler) {
            val response = getPasskeys(context, request, getPasskeysForDomain, accountManager)
            callback.onResult(response)
        }

        cancellationSignal.setOnCancelListener {
            PassLogger.w(TAG, "cancellationSignal received")
            job.cancel()
        }
    }

    private suspend fun getPasskeys(
        context: Context,
        request: BeginGetCredentialRequest,
        getPasskeysForDomain: GetPasskeysForDomain,
        accountManager: AccountManager
    ): BeginGetCredentialResponse? {
        val currentUser = accountManager.getPrimaryUserId().first()
        if (currentUser == null) {
            PassLogger.d(TAG, "No user found")
            return null
        }

        val domain = request.callingAppInfo?.origin ?: run {
            PassLogger.d(TAG, "Could not find origin in request")
            return BeginGetCredentialResponse(
                credentialEntries = emptyList(),
                actions = emptyList(),
            )
        }

        val passkeys = getPasskeysForDomain(domain)
        PassLogger.d(TAG, "Found ${passkeys.size} passkeys for domain $domain")

        val entries = mutableListOf<CredentialEntry>()

        for (option in request.beginGetCredentialOptions) {
            when (option) {
                is BeginGetPublicKeyCredentialOption -> {
                    entries.addAll(addOptionEntries(context, option, passkeys))
                }

                else -> {
                    PassLogger.d(TAG, "Only Passkey is supported")
                    continue
                }
            }
        }

        return BeginGetCredentialResponse(
            credentialEntries = entries,
            actions = emptyList(),
        )
    }

    private fun addOptionEntries(
        context: Context,
        option: BeginGetPublicKeyCredentialOption,
        passkeys: List<Passkey>
    ): List<CredentialEntry> = passkeys.map {
        PublicKeyCredentialEntry.Builder(
            context = context,
            username = it.userName,
            pendingIntent = createPendingIntent(context, it.id),
            beginGetPublicKeyCredentialOption = option
        ).setDisplayName(it.userDisplayName).setAutoSelectAllowed(false).build()
    }


    private fun createPendingIntent(
        context: Context,
        keyId: PasskeyId
    ): PendingIntent {
        val intent = SelectPasskeyActivity.createIntent(context, keyId)

        val requestCode = (1..9999).random()
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

}
