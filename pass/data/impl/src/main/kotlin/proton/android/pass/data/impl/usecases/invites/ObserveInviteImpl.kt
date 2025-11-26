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

package proton.android.pass.data.impl.usecases.invites

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.repositories.GroupInviteRepository
import proton.android.pass.data.api.repositories.UserInviteRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.invites.ObserveInvite
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.PendingInvite
import javax.inject.Inject

class ObserveInviteImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val userInviteRepository: UserInviteRepository,
    private val groupInviteRepository: GroupInviteRepository
) : ObserveInvite {

    override fun invoke(inviteToken: InviteToken): Flow<Option<PendingInvite>> = observeCurrentUser()
        .mapLatest { currentUser ->
            userInviteRepository.getInvite(currentUser.userId, inviteToken)
        }

    override fun invoke(inviteId: InviteId): Flow<Option<PendingInvite>> = observeCurrentUser()
        .flatMapLatest { currentUser ->
            groupInviteRepository.observePendingGroupInvite(currentUser.userId, inviteId)
                .mapLatest { it.toOption() }
        }

}
