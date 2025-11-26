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

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.extension.isOrganizationAdmin
import me.proton.core.user.domain.repository.UserRepository
import proton.android.pass.data.api.repositories.GroupInviteRepository
import proton.android.pass.data.api.usecases.RefreshGroupInvites
import proton.android.pass.domain.events.EventToken
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.NotificationManager
import javax.inject.Inject

class RefreshGroupInvitesImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val groupInviteRepository: GroupInviteRepository,
    private val userRepository: UserRepository,
    private val notificationManager: NotificationManager
) : RefreshGroupInvites {

    override suspend fun invoke(userId: UserId?, eventToken: EventToken?) {
        PassLogger.i(TAG, "Refreshing group invites started")

        runCatching {
            val currentUserId = userId ?: accountManager.getPrimaryUserId()
                .filterNotNull()
                .first()
            val currentUser = userRepository.getUser(currentUserId)
            if (currentUser.isOrganizationAdmin()) {
                groupInviteRepository.observePendingGroupInvites(
                    userId = currentUserId,
                    forceRefresh = true,
                    eventToken = eventToken
                ).first().lastOrNull()
            } else {
                null
            }
        }.onSuccess { invite ->
            if (invite != null) {
                PassLogger.i(TAG, "Group invites refreshed successfully")
                notificationManager.sendReceivedInviteNotification(invite)
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
