/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton Pass.
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

package proton.android.pass.data.impl.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.ClearUserSyncState
import proton.android.pass.data.impl.repositories.UserEventRepository
import javax.inject.Inject

class ClearUserSyncStateImpl @Inject constructor(
    private val shareRepository: ShareRepository,
    private val userEventRepository: UserEventRepository
) : ClearUserSyncState {

    override suspend fun invoke(userId: UserId) {
        userEventRepository.deleteLatestEventId(userId)
        shareRepository.deleteLocalSharesForUser(userId)
    }
}
