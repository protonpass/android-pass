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

package proton.android.pass.telemetry.impl

import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.data.api.repositories.LiveTelemetryRepository
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

interface LiveTelemetrySender {
    suspend fun sendEvents()
}

class LiveTelemetrySenderImpl @Inject constructor(
    private val repository: LiveTelemetryRepository,
    private val accountManager: AccountManager
) : LiveTelemetrySender {

    override suspend fun sendEvents() {
        val accounts = accountManager.getAccounts().firstOrNull() ?: run {
            PassLogger.i(TAG, "No accounts")
            return
        }

        accounts.forEach { account ->
            val userId = account.userId
            repository.flushPendingEvents(userId)
        }
    }

    companion object {
        private const val TAG = "LiveTelemetrySenderImpl"
    }
}
