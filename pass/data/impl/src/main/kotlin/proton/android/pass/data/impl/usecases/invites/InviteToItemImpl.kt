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
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.api.repositories.AddressPermission
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.GetInviteUserMode
import proton.android.pass.data.api.usecases.InviteUserMode
import proton.android.pass.data.api.usecases.invites.InviteToItem
import proton.android.pass.data.impl.crypto.EncryptItemsKeysForUser
import proton.android.pass.data.impl.remote.RemoteInviteDataSource
import proton.android.pass.data.impl.requests.CreateInviteKey
import proton.android.pass.data.impl.requests.CreateInviteRequest
import proton.android.pass.data.impl.requests.CreateInvitesRequest
import proton.android.pass.data.impl.requests.CreateNewUserInvitesRequest
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType
import javax.inject.Inject

class InviteToItemImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val shareRepository: ShareRepository,
    private val getInviteUserMode: GetInviteUserMode,
    private val encryptItemsKeysForUser: EncryptItemsKeysForUser,
    private val remoteInviteDataSource: RemoteInviteDataSource
) : InviteToItem {

    override suspend fun invoke(
        shareId: ShareId,
        itemId: ItemId,
        inviteAddresses: List<AddressPermission>
    ) {
        val userId = accountManager.getPrimaryUserId()
            .firstOrNull()
            ?: throw UserIdNotAvailableError()

        val existingUserInvitesAddresses = getUserInviteAddresses(userId, inviteAddresses)

        val inviterUserAddress = shareRepository.getAddressForShareId(userId, shareId)

        remoteInviteDataSource.sendInvites(
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
        inviteAddresses: List<AddressPermission>
    ): List<AddressPermission> = coroutineScope {
        val addressPermissionToInviteMode: List<Pair<AddressPermission, InviteUserMode>> =
            inviteAddresses.map { addressPermission ->
                async {
                    getInviteUserMode(userId, addressPermission.address).map { inviteUserMode ->
                        addressPermission to inviteUserMode
                    }
                }
            }.awaitAll().transpose().getOrThrow()

        // NewUserInvites are not allowed for item invites
        addressPermissionToInviteMode.mapNotNull { (addressPermission, inviteUserMode) ->
            addressPermission.takeIf { inviteUserMode == InviteUserMode.ExistingUser }
        }
    }

    private suspend fun createExistingUserInvites(
        shareId: ShareId,
        itemId: ItemId,
        existingUserInvitesAddresses: List<AddressPermission>,
        inviterUserAddress: UserAddress
    ): List<Deferred<CreateInviteRequest>> = coroutineScope {
        existingUserInvitesAddresses.map { existingUserInviteAddress ->
            async {
                val encryptedShareKeys = encryptItemsKeysForUser(
                    shareId = shareId,
                    itemId = itemId,
                    userAddress = inviterUserAddress,
                    targetEmail = existingUserInviteAddress.address
                ).getOrThrow()
                CreateInviteRequest(
                    keys = encryptedShareKeys.keys.map { encryptedInviteKey ->
                        CreateInviteKey(
                            key = encryptedInviteKey.key,
                            keyRotation = encryptedInviteKey.keyRotation
                        )
                    },
                    email = existingUserInviteAddress.address,
                    targetType = ShareType.Item.value,
                    shareRoleId = existingUserInviteAddress.shareRole.value,
                    itemId = itemId.id
                )
            }
        }
    }
}
