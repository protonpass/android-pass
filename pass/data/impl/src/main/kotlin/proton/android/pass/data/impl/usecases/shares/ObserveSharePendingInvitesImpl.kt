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

package proton.android.pass.data.impl.usecases.shares

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import proton.android.pass.data.api.repositories.ShareInvitesRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.data.api.usecases.shares.ObserveSharePendingInvites
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.shares.SharePendingInvite
import javax.inject.Inject

class ObserveSharePendingInvitesImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val observeShare: ObserveShare,
    private val shareInvitesRepository: ShareInvitesRepository
) : ObserveSharePendingInvites {

    override fun invoke(shareId: ShareId, itemId: ItemId?): Flow<List<SharePendingInvite>> =
        observeShare(shareId).flatMapLatest { share ->
            if (share.isAdmin) {
                observeCurrentUser().flatMapLatest { user ->
                    shareInvitesRepository.observeSharePendingInvites(user.userId, shareId)
                        .mapLatest { sharePendingInvites ->
                            sharePendingInvites.filter { sharePendingInvite ->
                                if (itemId == null) true
                                else sharePendingInvite.targetId == itemId.id
                            }
                        }
                }
            } else {
                flowOf(emptyList())
            }
        }

}
