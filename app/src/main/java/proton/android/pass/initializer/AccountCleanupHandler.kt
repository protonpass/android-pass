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

package proton.android.pass.initializer

import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.usecases.ClearUserData
import proton.android.pass.data.api.usecases.GetReadyAccounts
import proton.android.pass.data.api.usecases.ResetAppToDefaults
import proton.android.pass.domain.UserId
import proton.android.pass.log.api.PassLogger

class AccountCleanupHandler(
    private val itemSyncStatusRepository: ItemSyncStatusRepository,
    private val clearUserData: ClearUserData,
    private val getReadyAccounts: GetReadyAccounts,
    private val resetAppToDefaults: ResetAppToDefaults,
    private val cleanupLastUser: suspend () -> Unit
) {

    suspend operator fun invoke(userId: UserId) {
        itemSyncStatusRepository.clear()

        safeRunCatching { clearUserData(userId) }
            .onSuccess { PassLogger.i(TAG, "Cleared user data") }
            .onFailure { PassLogger.i(TAG, it, "Error clearing user data") }

        val activeAccounts = getReadyAccounts()
        PassLogger.i(TAG, "Active accounts remaining: ${activeAccounts.size}")

        if (activeAccounts.isEmpty()) {
            PassLogger.i(TAG, "No more active accounts - performing final cleanup")

            safeRunCatching { cleanupLastUser() }
                .onSuccess { PassLogger.i(TAG, "Database cleanup completed") }
                .onFailure {
                    PassLogger.w(TAG, "Error during database cleanup")
                    PassLogger.w(TAG, it)
                }

            resetAppToDefaults()
        }
    }

    private companion object {
        private const val TAG = "AccountListenerInitializer"
    }
}
