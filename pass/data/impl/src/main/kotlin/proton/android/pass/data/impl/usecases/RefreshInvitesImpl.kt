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
import proton.android.pass.data.api.repositories.InviteRepository
import proton.android.pass.data.api.usecases.RefreshInvites
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.NotificationManager
import javax.inject.Inject

class RefreshInvitesImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val inviteRepository: InviteRepository,
    private val notificationManager: NotificationManager
) : RefreshInvites {

    override suspend fun invoke(userId: UserId?) {
        PassLogger.i(TAG, "Refreshing invites started")

        runCatching {
            val currentUserId = userId ?: accountManager.getPrimaryUserId()
                .filterNotNull()
                .first()

            if (inviteRepository.refreshInvites(currentUserId)) {
                inviteRepository.observeInvites(currentUserId)
                    .first()
                    .lastOrNull()
            } else null
        }.onSuccess { pendingInvite ->
            PassLogger.i(TAG, "Invites refreshed successfully")
            pendingInvite?.let(notificationManager::sendReceivedInviteNotification)
        }.onFailure { error ->
            PassLogger.i(TAG, "Error refreshing invites")
            PassLogger.w(TAG, error)
        }
    }

    private companion object {

        private const val TAG = "RefreshInvitesImpl"

    }

}
