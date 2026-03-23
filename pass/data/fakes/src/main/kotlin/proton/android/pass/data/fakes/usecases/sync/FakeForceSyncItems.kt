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

package proton.android.pass.data.fakes.usecases.sync

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.sync.ForceSyncItems
import proton.android.pass.data.api.usecases.sync.ForceSyncResult
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeForceSyncItems @Inject constructor() : ForceSyncItems {

    data class Invocation(
        val userId: UserId,
        val shareIds: Set<ShareId>,
        val hasInactiveShares: Boolean,
        val hasInvalidGroupShares: Boolean,
        val hasInvalidAddressShares: Boolean
    )

    val invocations = mutableListOf<Invocation>()
    private var result: ForceSyncResult = ForceSyncResult.Success

    fun setResult(value: ForceSyncResult) {
        result = value
    }

    override suspend fun invoke(
        userId: UserId,
        shareIds: Set<ShareId>,
        hasInactiveShares: Boolean,
        hasInvalidGroupShares: Boolean,
        hasInvalidAddressShares: Boolean
    ): ForceSyncResult {
        invocations += Invocation(
            userId = userId,
            shareIds = shareIds,
            hasInactiveShares = hasInactiveShares,
            hasInvalidGroupShares = hasInvalidGroupShares,
            hasInvalidAddressShares = hasInvalidAddressShares
        )
        return result
    }
}
