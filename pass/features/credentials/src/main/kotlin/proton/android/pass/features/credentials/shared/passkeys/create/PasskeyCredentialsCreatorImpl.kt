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

package proton.android.pass.features.credentials.shared.passkeys.create

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.credentials.provider.CreateEntry
import kotlinx.coroutines.flow.first
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import proton.android.pass.features.credentials.passkeys.ui.CreatePasskeyActivity
import proton.android.pass.features.credentials.shared.passkeys.events.PasskeyCredentialsTelemetryEvent
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

internal class PasskeyCredentialsCreatorImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val telemetryManager: TelemetryManager
) : PasskeyCredentialsCreator {

    override suspend fun create(context: Context): List<CreateEntry> = accountManager.getPrimaryAccount()
        .first()
        ?.let { account ->
            CreateEntry(
                accountName = account.username.orEmpty(),
                pendingIntent = createPendingIntent(context)
            )
        }
        ?.let(::listOf)
        ?.also {
            telemetryManager.sendEvent(PasskeyCredentialsTelemetryEvent.CreatePromptDisplay)
        }
        ?: emptyList()


    private fun createPendingIntent(context: Context): PendingIntent =
        Intent(context, CreatePasskeyActivity::class.java)
            .apply { setPackage(context.packageName) }
            .let { intent ->
                PendingIntent.getActivity(
                    context,
                    PENDING_INTENT_REQUEST_CODE,
                    intent,
                    PENDING_INTENT_FLAGS
                )
            }

    private companion object {

        private const val PENDING_INTENT_REQUEST_CODE = 1

        private const val PENDING_INTENT_FLAGS = PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

    }

}
