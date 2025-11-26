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

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.common.api.transpose
import proton.android.pass.data.api.errors.NewUsersInviteError
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.api.repositories.InviteTarget
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.GetInviteUserMode
import proton.android.pass.data.api.usecases.InviteUserMode
import proton.android.pass.data.api.usecases.invites.InviteToItem
import proton.android.pass.data.impl.crypto.EncryptItemsKeysForUser
import proton.android.pass.data.impl.remote.RemoteUserInviteDataSource
import proton.android.pass.data.impl.requests.CreateInviteKey
import proton.android.pass.data.impl.requests.CreateInviteRequest
import proton.android.pass.data.impl.requests.CreateInvitesRequest
import proton.android.pass.data.impl.requests.CreateNewUserInvitesRequest
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.ShareType
import javax.inject.Inject

class InviteToItemImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val shareRepository: ShareRepository,
    private val getInviteUserMode: GetInviteUserMode,
    private val encryptItemsKeysForUser: EncryptItemsKeysForUser,
    private val remoteUserInviteDataSource: RemoteUserInviteDataSource
) : InviteToItem {

    override suspend fun invoke(
        shareId: ShareId,
        itemId: ItemId,
        inviteTargets: List<InviteTarget>
    ) {
        val userId = accountManager.getPrimaryUserId()
            .firstOrNull()
            ?: throw UserIdNotAvailableError()

        val existingUserInvitesAddresses = getUserInviteAddresses(userId, inviteTargets)

        val inviterUserAddress = shareRepository.getAddressForShareId(userId, shareId)

        remoteUserInviteDataSource.sendInvites(
            userId = userId,
            shareId = shareId,
            existingUserRequests = CreateInvitesRequest(
                invites = createExistingUserInvites(
                    shareId = shareId,
                    itemId = itemId,
                    existingUserInvitesAddresses = existingUserInvitesAddresses,
                    inviterUserAddress = inviterUserAddress
                ).awaitAll()
            ),
            newUserRequests = CreateNewUserInvitesRequest(invites = emptyList())
        )
    }

    private suspend fun getUserInviteAddresses(
        userId: UserId,
        inviteTargets: List<InviteTarget>
    ): List<Pair<String, ShareRole>> = coroutineScope {
        val inviteModeToInviteAddresses =
            inviteTargets.map { inviteTarget ->
                async {
                    getInviteUserMode(userId, inviteTarget.email).map { inviteUserMode ->
                        Triple(inviteTarget.email, inviteTarget.shareRole, inviteUserMode)
                    }
                }
            }
                .awaitAll()
                .transpose()
                .getOrThrow()
                .groupBy { (_, _, inviteUserMode) -> inviteUserMode }
                .mapValues {
                    it.value.map { (email, role, _) -> email to role }
                }

        // NewUserInvites are not allowed for item invites
        inviteModeToInviteAddresses[InviteUserMode.NewUser]?.let { newUserAddresses ->
            throw NewUsersInviteError(newUserAddresses)
        }

        inviteModeToInviteAddresses[InviteUserMode.ExistingUser].orEmpty()
    }

    private suspend fun createExistingUserInvites(
        shareId: ShareId,
        itemId: ItemId,
        existingUserInvitesAddresses: List<Pair<String, ShareRole>>,
        inviterUserAddress: UserAddress
    ): List<Deferred<CreateInviteRequest>> = coroutineScope {
        existingUserInvitesAddresses.map { (email, role) ->
            async {
                val encryptedShareKeys = encryptItemsKeysForUser(
                    shareId = shareId,
                    itemId = itemId,
                    userAddress = inviterUserAddress,
                    targetEmail = email
                ).getOrThrow()
                CreateInviteRequest(
                    keys = encryptedShareKeys.keys.map { encryptedInviteKey ->
                        CreateInviteKey(
                            key = encryptedInviteKey.key,
                            keyRotation = encryptedInviteKey.keyRotation
                        )
                    },
                    email = email,
                    targetType = ShareType.Item.value,
                    shareRoleId = role.value,
                    itemId = itemId.id
                )
            }
        }
    }
}
