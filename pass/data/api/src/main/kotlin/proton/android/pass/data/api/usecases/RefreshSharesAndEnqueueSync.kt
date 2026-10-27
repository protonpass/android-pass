/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.data.api.usecases

import proton.android.pass.data.api.repositories.SyncReason
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.UserId

interface RefreshSharesAndEnqueueSync {
    suspend operator fun invoke(
        userId: UserId,
        syncType: SyncType,
        workerOrigin: String = "unknown",
        syncReason: SyncReason = SyncReason.Default,
        deferSuccess: Boolean = false
    ): RefreshSharesResult

    enum class SyncType {
        INCREMENTAL,
        FULL,
        FULL_BACKGROUND
    }
}

sealed interface RefreshSharesResult {
    data class SharesFound(
        val shareIds: Set<ShareId>,
        val isWorkerEnqueued: Boolean,
        val hasInactiveShares: Boolean,
        val hasInvalidGroupShares: Boolean
    ) : RefreshSharesResult
    data class NoSharesVaultCreated(
        val hasInactiveShares: Boolean = false,
        val hasInvalidGroupShares: Boolean = false
    ) : RefreshSharesResult
    data class NoSharesSkipped(
        val hasInactiveShares: Boolean = false,
        val hasInvalidGroupShares: Boolean = false
    ) : RefreshSharesResult
}
