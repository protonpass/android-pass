/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import proton.android.pass.data.api.repositories.GroupInviteRepository
import proton.android.pass.data.api.repositories.UserInviteRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.RejectInvite
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.InviteToken
import proton.android.pass.notifications.api.NotificationManager
import javax.inject.Inject

class RejectInviteImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val userInviteRepository: UserInviteRepository,
    private val groupInviteRepository: GroupInviteRepository,
    private val notificationManager: NotificationManager
) : RejectInvite {

    override suspend fun invoke(inviteToken: InviteToken) {
        val user = observeCurrentUser().first()

        userInviteRepository.getInvite(user.userId, inviteToken).value()
            ?.let { pendingInvite ->
                userInviteRepository.rejectInvite(user.userId, inviteToken)
                notificationManager.removeReceivedInviteNotification(pendingInvite)
            }
    }

    override suspend fun invoke(inviteId: InviteId) {
        val (userId, pendingInvite) = observeCurrentUser()
            .flatMapLatest { user ->
                groupInviteRepository.observePendingGroupInvite(user.userId, inviteId)
                    .map { user.userId to it }
            }
            .first()
        if (pendingInvite != null) {
            groupInviteRepository.rejectGroupInvite(userId, pendingInvite.inviteToken)
        } else {
            error("No pending invite found")
        }
    }
}
