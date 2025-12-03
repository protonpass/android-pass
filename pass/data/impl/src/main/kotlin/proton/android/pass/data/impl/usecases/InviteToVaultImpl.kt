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
import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.transpose
import proton.android.pass.data.api.repositories.InviteTarget
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.repositories.UserInviteRepository
import proton.android.pass.data.api.usecases.GetInviteUserMode
import proton.android.pass.data.api.usecases.InviteToVault
import proton.android.pass.data.api.usecases.InviteUserMode
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class InviteToVaultImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val userInviteRepository: UserInviteRepository,
    private val shareRepository: ShareRepository,
    private val getInviteUserMode: GetInviteUserMode
) : InviteToVault {

    @Suppress("ReturnCount")
    override suspend fun invoke(
        userId: UserId?,
        shareId: ShareId,
        inviteTargets: List<InviteTarget>
    ): Result<Unit> = coroutineScope {
        val id = userId ?: run {
            val primaryUserId = accountManager.getPrimaryUserId().firstOrNull()
            if (primaryUserId == null) {
                PassLogger.w(TAG, "No primary user")
                return@coroutineScope Result.failure(IllegalStateException("No primary user"))
            }
            primaryUserId
        }

        val inviteUserModes: Map<String, InviteUserMode> = inviteTargets.map { userTarget ->
            async {
                getInviteUserMode(
                    id,
                    userTarget.email
                ).map { mode -> userTarget.email to mode }
            }
        }.awaitAll().transpose().getOrElse {
            PassLogger.w(TAG, "Error obtaining inviteUserModes")
            PassLogger.w(TAG, it)
            return@coroutineScope Result.failure(it)
        }.toMap()

        val (newUserInvites, existingUserInvites) = inviteTargets.partition {
            inviteUserModes[it.email] == InviteUserMode.NewUser
        }

        val existing = existingUserInvites.size
        val new = newUserInvites.size

        PassLogger.i(TAG, "Sending $existing existing user invites and $new new user invites")

        runCatching {
            if (existingUserInvites.isNotEmpty()) {
                userInviteRepository.sendInvitesToExistingUsers(
                    id,
                    shareId,
                    existingUserInvites
                )
            }
            if (newUserInvites.isNotEmpty()) {
                userInviteRepository.sendInvitesToNewUsers(id, shareId, newUserInvites)
            }
        }.onSuccess {
            PassLogger.i(TAG, "Invites sent successfully. Refreshing share")
            runCatching {
                shareRepository.refreshShare(id, shareId)
            }.onSuccess {
                PassLogger.d(TAG, "Share refreshed successfully")
            }.onFailure {
                PassLogger.w(TAG, "Error refreshing shares")
                PassLogger.w(TAG, it)
            }
        }
    }

    companion object {
        private const val TAG = "InviteToVaultImpl"
    }
}
