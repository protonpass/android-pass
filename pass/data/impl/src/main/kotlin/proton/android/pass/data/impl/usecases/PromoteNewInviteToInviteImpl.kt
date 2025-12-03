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

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.transpose
import proton.android.pass.data.api.repositories.InviteTarget
import proton.android.pass.data.api.repositories.ShareInvitesRepository
import proton.android.pass.data.api.repositories.UserInviteRepository
import proton.android.pass.data.api.repositories.UserTarget
import proton.android.pass.data.api.usecases.GetInviteUserMode
import proton.android.pass.data.api.usecases.InviteUserMode
import proton.android.pass.data.api.usecases.PromoteNewInviteToInvite
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.shares.SharePendingInvite
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class PromoteNewInviteToInviteImpl @Inject constructor(
    private val shareInvitesRepository: ShareInvitesRepository,
    private val userInviteRepository: UserInviteRepository,
    private val getInviteUserMode: GetInviteUserMode
) : PromoteNewInviteToInvite {

    override suspend fun invoke(userId: UserId, shareId: ShareId) = coroutineScope {
        val pendingInvites = shareInvitesRepository.observeSharePendingInvites(userId, shareId).first()
        val newUserInvites = pendingInvites.filterIsInstance<SharePendingInvite.NewUser>()

        if (newUserInvites.isEmpty()) {
            PassLogger.d(TAG, "No new user invites to promote")
            return@coroutineScope
        }

        val inviteTargets: List<InviteTarget> = newUserInvites.map { newUserInvite ->
            UserTarget(
                email = newUserInvite.email,
                shareRole = newUserInvite.role
            )
        }

        val inviteUserModes: Map<String, InviteUserMode> = inviteTargets.map { userTarget ->
            async {
                getInviteUserMode(
                    userId,
                    userTarget.email
                ).map { mode -> userTarget.email to mode }
            }
        }.awaitAll().transpose().getOrElse {
            PassLogger.w(TAG, "Error obtaining inviteUserModes")
            PassLogger.w(TAG, it)
            return@coroutineScope
        }.toMap()

        val (newUserInvitesList, existingUserInvites) = inviteTargets.partition {
            inviteUserModes[it.email] == InviteUserMode.NewUser
        }

        val existing = existingUserInvites.size
        val new = newUserInvitesList.size

        PassLogger.i(TAG, "Promoting invites: $existing existing user invites and $new new user invites")

        if (existingUserInvites.isNotEmpty()) {
            userInviteRepository.sendInvitesToExistingUsers(
                userId,
                shareId,
                existingUserInvites
            )
        }
        if (newUserInvitesList.isNotEmpty()) {
            userInviteRepository.sendInvitesToNewUsers(userId, shareId, newUserInvitesList)
        }
    }

    companion object {
        private const val TAG = "PromoteNewInviteToInviteImpl"
    }
}
