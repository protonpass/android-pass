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

package proton.android.pass.features.credentials.shared.passkeys.search

import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.credentials.provider.Action
import androidx.credentials.provider.BeginGetPublicKeyCredentialOption
import androidx.credentials.provider.PublicKeyCredentialEntry
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import proton.android.pass.biometry.NeedsBiometricAuth
import proton.android.pass.data.api.usecases.passkeys.GetPasskeysForDomain
import proton.android.pass.data.api.usecases.passkeys.PasskeyItem
import proton.android.pass.features.credentials.R
import proton.android.pass.features.credentials.passkeys.selection.ui.PasskeyCredentialSelectionActivity
import proton.android.pass.features.credentials.passkeys.usage.ui.PasskeyCredentialUsageActivity
import proton.android.pass.features.credentials.shared.passkeys.domain.PasskeyCredential
import proton.android.pass.features.credentials.shared.passkeys.events.PasskeyCredentialsTelemetryEvent
import proton.android.pass.log.api.PassLogger
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
internal class PasskeyCredentialsSearcherImpl @Inject constructor(
    private val getPasskeysForDomain: GetPasskeysForDomain,
    private val needsBiometricAuth: NeedsBiometricAuth,
    private val telemetryManager: TelemetryManager
) : PasskeyCredentialsSearcher {

    private val requestCodes = mutableSetOf<Int>()

    private val requestCode: Int
        get() {
            var newRequestCode: Int
            do {
                newRequestCode = (REQUEST_CODE_RANGE_START..REQUEST_CODE_RANGE_END).random()
            } while (newRequestCode in requestCodes)
            requestCodes.add(newRequestCode)
            return newRequestCode
        }

    private val jsonParser = Json { ignoreUnknownKeys = true }

    override suspend fun search(
        context: Context,
        option: BeginGetPublicKeyCredentialOption
    ): Pair<List<PublicKeyCredentialEntry>, Action>? {
        val passkeyCredential = createPasskeyCredential(option) ?: return null

        val passkeyCredentialEntries = createPasskeyCredentialEntries(
            credential = passkeyCredential,
            context = context,
            option = option,
            isBiometricAuthRequired = needsBiometricAuth().first()
        )

        val passkeyCredentialAction = createPasskeyCredentialAction(
            context = context,
            option = option,
            credential = passkeyCredential
        )

        return Pair(passkeyCredentialEntries, passkeyCredentialAction)
            .also { telemetryManager.sendEvent(PasskeyCredentialsTelemetryEvent.DisplaySuggestions) }
    }

    private fun createPasskeyCredential(option: BeginGetPublicKeyCredentialOption): PasskeyCredential? = runCatching {
        jsonParser.decodeFromString<PasskeyCredential>(option.requestJson)
    }.getOrElse { error ->
        PassLogger.w(TAG, "Error parsing Passkey option JSON request")
        PassLogger.w(TAG, error)

        null
    }

    private suspend fun createPasskeyCredentialEntries(
        credential: PasskeyCredential,
        context: Context,
        option: BeginGetPublicKeyCredentialOption,
        isBiometricAuthRequired: Boolean
    ) = getPasskeysForDomain(
        domain = credential.domain,
        selection = credential.passkeySelection
    ).map { passkeyItem ->
        PublicKeyCredentialEntry.Builder(
            context = context,
            username = passkeyItem.passkey.userName,
            beginGetPublicKeyCredentialOption = option,
            pendingIntent = createPasskeyPendingIntent(
                context = context,
                credential = credential,
                option = option,
                passkeyItem = passkeyItem,
                isBiometricAuthRequired = isBiometricAuthRequired
            )
        )
            .setDisplayName(passkeyItem.itemTitle)
            .setAutoSelectAllowed(false)
            .build()
    }

    private fun createPasskeyPendingIntent(
        context: Context,
        credential: PasskeyCredential,
        option: BeginGetPublicKeyCredentialOption,
        passkeyItem: PasskeyItem,
        isBiometricAuthRequired: Boolean
    ) = if (isBiometricAuthRequired) {
        PasskeyCredentialSelectionActivity.createPasskeyCredentialIntent(
            context = context,
            passkeyItem = passkeyItem
        )
    } else {
        PasskeyCredentialUsageActivity.createPasskeyCredentialIntent(
            context = context,
            origin = credential.domain,
            option = option,
            passkeyItem = passkeyItem
        )
    }.let { intent ->
        PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PENDING_INTENT_FLAGS
        )
    }

    private fun createPasskeyCredentialAction(
        context: Context,
        option: BeginGetPublicKeyCredentialOption,
        credential: PasskeyCredential
    ) = PasskeyCredentialSelectionActivity.createPasskeyCredentialIntent(
        context = context,
        option = option,
        origin = credential.domain
    )
        .let { intent ->
            PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PENDING_INTENT_FLAGS
            )
        }
        .let { pendingIntent ->
            Action(
                title = context.getString(R.string.passkey_credential_selection_action_title),
                subtitle = context.getString(R.string.passkey_credential_selection_action_subtitle),
                pendingIntent = pendingIntent
            )
        }

    private companion object {

        private const val TAG = "PassPasskeyCredentialsSearcherImpl"

        private const val REQUEST_CODE_RANGE_START = 1

        private const val REQUEST_CODE_RANGE_END = 9999

        private const val PENDING_INTENT_FLAGS = PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

    }

}
