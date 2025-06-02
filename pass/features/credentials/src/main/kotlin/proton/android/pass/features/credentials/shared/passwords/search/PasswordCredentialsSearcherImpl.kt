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

package proton.android.pass.features.credentials.shared.passwords.search

import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.credentials.provider.Action
import androidx.credentials.provider.BeginGetPasswordOption
import androidx.credentials.provider.CallingAppInfo
import androidx.credentials.provider.PasswordCredentialEntry
import kotlinx.coroutines.flow.first
import proton.android.pass.autofill.api.suggestions.PackageNameUrlSuggestionAdapter
import proton.android.pass.biometry.NeedsBiometricAuth
import proton.android.pass.data.api.repositories.AssetLinkRepository
import proton.android.pass.data.api.usecases.Suggestion
import proton.android.pass.data.api.usecases.credentials.passwords.GetPasswordCredentialItems
import proton.android.pass.domain.credentials.PasswordCredentialItem
import proton.android.pass.domain.entity.PackageName
import proton.android.pass.features.credentials.R
import proton.android.pass.features.credentials.passwords.selection.ui.PasswordCredentialSelectionActivity
import proton.android.pass.features.credentials.passwords.usage.ui.PasswordCredentialUsageActivity
import proton.android.pass.features.credentials.shared.passwords.events.PasswordCredentialsTelemetryEvent
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
internal class PasswordCredentialsSearcherImpl @Inject constructor(
    private val assetLinkRepository: AssetLinkRepository,
    private val packageNameUrlSuggestionAdapter: PackageNameUrlSuggestionAdapter,
    private val getPasswordCredentialItems: GetPasswordCredentialItems,
    private val needsBiometricAuth: NeedsBiometricAuth,
    private val telemetryManager: TelemetryManager
) : PasswordCredentialsSearcher {

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

    override suspend fun search(
        context: Context,
        callingAppInfo: CallingAppInfo?,
        option: BeginGetPasswordOption
    ): Pair<List<PasswordCredentialEntry>, Action>? {
        val callingPackageName = callingAppInfo?.packageName.orEmpty()

        val url = assetLinkRepository.observeByPackageName(callingPackageName)
            .first()
            .firstOrNull()
            ?.website
            .orEmpty()

        val suggestion = packageNameUrlSuggestionAdapter.adapt(
            packageName = PackageName(value = callingPackageName),
            url = url
        ).toSuggestion()

        val passwordCredentialEntries = createPasswordCredentialEntries(
            context = context,
            option = option,
            isBiometricAuthRequired = needsBiometricAuth().first(),
            suggestion = suggestion
        )

        val passwordCredentialAction = createPasswordCredentialAction(
            context = context,
            suggestion = suggestion
        )

        return Pair(passwordCredentialEntries, passwordCredentialAction)
            .also { telemetryManager.sendEvent(PasswordCredentialsTelemetryEvent.DisplaySuggestions) }
    }

    private suspend fun createPasswordCredentialEntries(
        context: Context,
        suggestion: Suggestion,
        option: BeginGetPasswordOption,
        isBiometricAuthRequired: Boolean
    ) = getPasswordCredentialItems(suggestion).map { passwordCredentialItem ->
        PasswordCredentialEntry.Builder(
            context = context,
            username = passwordCredentialItem.username,
            beginGetPasswordOption = option,
            pendingIntent = createPasswordCredentialPendingIntent(
                isBiometricAuthRequired = isBiometricAuthRequired,
                context = context,
                passwordCredentialItem = passwordCredentialItem,
                suggestion = suggestion
            )
        )
            .setDisplayName(passwordCredentialItem.displayName)
            .setAutoSelectAllowed(false)
            .build()
    }

    private fun createPasswordCredentialPendingIntent(
        isBiometricAuthRequired: Boolean,
        context: Context,
        passwordCredentialItem: PasswordCredentialItem,
        suggestion: Suggestion
    ) = if (isBiometricAuthRequired) {
        PasswordCredentialSelectionActivity.createPasswordCredentialIntent(
            context = context,
            passwordCredentialItem = passwordCredentialItem,
            suggestion = suggestion
        )
    } else {
        PasswordCredentialUsageActivity.createPasswordCredentialIntent(
            context = context,
            passwordCredentialItem = passwordCredentialItem
        )
    }.let { intent ->
        PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PENDING_INTENT_FLAGS
        )
    }

    private fun createPasswordCredentialAction(context: Context, suggestion: Suggestion) =
        PasswordCredentialSelectionActivity.createPasswordCredentialIntent(
            context = context,
            suggestion = suggestion
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
                    title = context.getString(R.string.password_credential_selection_action_title),
                    subtitle = context.getString(R.string.password_credential_selection_action_subtitle),
                    pendingIntent = pendingIntent
                )
            }

    private companion object {

        private const val REQUEST_CODE_RANGE_START = 1

        private const val REQUEST_CODE_RANGE_END = 9999

        private const val PENDING_INTENT_FLAGS = PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

    }

}
