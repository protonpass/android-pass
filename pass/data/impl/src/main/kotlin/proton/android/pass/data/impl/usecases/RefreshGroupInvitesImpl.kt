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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.extension.isOrganizationUser
import me.proton.core.user.domain.repository.UserRepository
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.repositories.GroupInviteRepository
import proton.android.pass.data.api.repositories.GroupRepository
import proton.android.pass.data.api.usecases.RefreshGroupInvites
import proton.android.pass.domain.GroupId
import proton.android.pass.domain.PendingGroupInvite
import proton.android.pass.domain.PendingInvite
import proton.android.pass.domain.PendingUserInvite
import proton.android.pass.domain.events.EventToken
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.InviteNotificationModel
import proton.android.pass.notifications.api.NotificationManager
import javax.inject.Inject

class RefreshGroupInvitesImpl @Inject constructor(
    private val groupInviteRepository: GroupInviteRepository,
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val notificationManager: NotificationManager
) : RefreshGroupInvites {

    override suspend fun invoke(userId: UserId, eventToken: EventToken?) {
        PassLogger.i(TAG, "Refreshing group invites started")

        safeRunCatching {
            val currentUser = userRepository.getUser(userId)
            if (!currentUser.isOrganizationUser()) return@safeRunCatching null

            val invite = groupInviteRepository.observePendingGroupInvites(
                userId = userId,
                forceRefresh = true,
                eventToken = eventToken
            ).first().lastOrNull() ?: return@safeRunCatching null

            when (invite) {
                is PendingGroupInvite -> {
                    val groupName = safeRunCatching {
                        groupRepository.retrieveGroup(
                            userId = userId,
                            groupId = GroupId(invite.invitedGroupId)
                        )?.name
                    }.getOrNull() ?: invite.invitedEmail

                    when (invite) {
                        is PendingInvite.GroupItem ->
                            InviteNotificationModel.GroupItem(invite.inviterEmail, groupName)
                        is PendingInvite.GroupVault ->
                            InviteNotificationModel.GroupVault(invite.inviterEmail, groupName)
                    }
                }
                is PendingUserInvite -> null
            }
        }.onSuccess { model ->
            if (model != null) {
                PassLogger.i(TAG, "Group invites refreshed successfully")
                notificationManager.sendReceivedInviteNotification(model)
            } else {
                PassLogger.i(TAG, "No pending group invites")
            }
        }.onFailure { error ->
            PassLogger.i(TAG, "Error refreshing group invites")
            PassLogger.w(TAG, error)
        }
    }

    private companion object {

        private const val TAG = "RefreshGroupInvitesImpl"

    }

}
