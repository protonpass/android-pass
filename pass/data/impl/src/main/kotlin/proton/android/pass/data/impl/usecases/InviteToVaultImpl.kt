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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.common.api.transpose
import proton.android.pass.data.api.repositories.InviteTarget
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.GetInviteUserMode
import proton.android.pass.data.api.usecases.InviteToVault
import proton.android.pass.data.api.usecases.InviteUserMode
import proton.android.pass.data.impl.crypto.EncryptShareKeysForUser
import proton.android.pass.data.impl.crypto.NewUserInviteSignatureManager
import proton.android.pass.data.impl.remote.RemoteUserInviteDataSource
import proton.android.pass.data.impl.repositories.ShareKeyRepository
import proton.android.pass.data.impl.requests.CreateInviteKey
import proton.android.pass.data.impl.requests.CreateInviteRequest
import proton.android.pass.data.impl.requests.CreateInvitesRequest
import proton.android.pass.data.impl.requests.CreateNewUserInviteRequest
import proton.android.pass.data.impl.requests.CreateNewUserInvitesRequest
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class InviteToVaultImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val encryptShareKeysForUser: EncryptShareKeysForUser,
    private val shareKeyRepository: ShareKeyRepository,
    private val remoteUserInviteDataSource: RemoteUserInviteDataSource,
    private val shareRepository: ShareRepository,
    private val newUserInviteSignatureManager: NewUserInviteSignatureManager,
    private val getInviteUserMode: GetInviteUserMode
) : InviteToVault {

    @Suppress("ReturnCount")
    override suspend fun invoke(
        userId: UserId?,
        shareId: ShareId,
        inviteTargets: List<InviteTarget>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val id = userId ?: run {
            val primaryUserId = accountManager.getPrimaryUserId().firstOrNull()
            if (primaryUserId == null) {
                PassLogger.w(TAG, "No primary user")
                return@withContext Result.failure(IllegalStateException("No primary user"))
            }
            primaryUserId
        }

        val inviterUserAddress = runCatching {
            shareRepository.getAddressForShareId(id, shareId)
        }.getOrElse {
            PassLogger.w(TAG, "Error obtaining inviterUserAddress")
            PassLogger.w(TAG, it)
            return@withContext Result.failure(it)
        }
        val share = runCatching {
            shareRepository.getById(id, shareId)
        }.getOrElse {
            PassLogger.w(TAG, "Error obtaining share")
            PassLogger.w(TAG, it)
            return@withContext Result.failure(it)
        }

        generateInvites(
            userId = id,
            shareId = shareId,
            groupEmail = share.groupEmail,
            inviterUserAddress = inviterUserAddress,
            inviteTargets = inviteTargets
        ).mapCatching { (existingUserInvites, newUserInvites) ->
            val existing = existingUserInvites.invites.size
            val new = newUserInvites.invites.size

            PassLogger.i(TAG, "Sending $existing existing user invites and $new new user invites")
            remoteUserInviteDataSource.sendInvites(
                userId = id,
                shareId = shareId,
                existingUserRequests = existingUserInvites,
                newUserRequests = newUserInvites
            )
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

    private suspend fun generateInvites(
        userId: UserId,
        shareId: ShareId,
        groupEmail: String?,
        inviterUserAddress: UserAddress,
        inviteTargets: List<InviteTarget>
    ): Result<Pair<CreateInvitesRequest, CreateNewUserInvitesRequest>> = withContext(Dispatchers.IO) {
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
            return@withContext Result.failure(it)
        }.toMap()

        val (newUserInvites, existingUserInvites) = inviteTargets.partition {
            inviteUserModes[it.email] == InviteUserMode.NewUser
        }

        val newUserInvitesRequests = newUserInvites.map {
            async {
                buildNewUserRequest(
                    userId = userId,
                    shareId = shareId,
                    groupEmail = groupEmail,
                    address = inviterUserAddress,
                    targetEmail = it.email,
                    shareRole = it.shareRole
                )
            }
        }.awaitAll().transpose().getOrElse {
            PassLogger.w(TAG, "Error creating newUserInvites")
            PassLogger.w(TAG, it)
            return@withContext Result.failure(it)
        }

        val existingUserInvitesRequests = existingUserInvites.map {
            async {
                buildExistingUserRequest(
                    shareId = shareId,
                    groupEmail = groupEmail,
                    address = inviterUserAddress,
                    targetEmail = it.email,
                    shareRole = it.shareRole
                )
            }
        }.awaitAll().transpose().getOrElse {
            PassLogger.w(TAG, "Error creating existingUserInvites")
            PassLogger.w(TAG, it)
            return@withContext Result.failure(it)
        }

        val existing = CreateInvitesRequest(existingUserInvitesRequests)
        val new = CreateNewUserInvitesRequest(newUserInvitesRequests)

        return@withContext Result.success(existing to new)
    }

    @Suppress("ReturnCount")
    private suspend fun buildExistingUserRequest(
        shareId: ShareId,
        groupEmail: String?,
        address: UserAddress,
        targetEmail: String,
        shareRole: ShareRole
    ): Result<CreateInviteRequest> {
        val encryptedKeys = encryptShareKeysForUser(
            userAddress = address,
            shareId = shareId,
            groupEmail = groupEmail,
            targetEmail = targetEmail
        ).getOrElse {
            return Result.failure(it)
        }

        val request = CreateInviteRequest(
            keys = encryptedKeys.keys.map {
                CreateInviteKey(
                    key = it.key,
                    keyRotation = it.keyRotation
                )
            },
            email = targetEmail,
            targetType = TARGET_TYPE_VAULT,
            shareRoleId = shareRole.value
        )

        return Result.success(request)
    }

    @Suppress("LongParameterList")
    private suspend fun buildNewUserRequest(
        userId: UserId,
        shareId: ShareId,
        groupEmail: String?,
        address: UserAddress,
        targetEmail: String,
        shareRole: ShareRole
    ): Result<CreateNewUserInviteRequest> {

        val vaultKeyList = shareKeyRepository.getShareKeys(
            userId = userId,
            addressId = address.addressId,
            shareId = shareId,
            groupEmail = groupEmail,
            forceRefresh = false
        ).firstOrNull()
            ?: return Result.failure(IllegalStateException("No ShareKey found for share"))

        val vaultKey = vaultKeyList.maxByOrNull { it.rotation }
            ?: return Result.failure(IllegalStateException("ShareKey list is empty"))

        val signature = newUserInviteSignatureManager.create(
            inviterUserAddress = address,
            email = targetEmail,
            inviteKey = vaultKey
        ).getOrElse {
            PassLogger.w(TAG, "Failed to create new user invite signature")
            PassLogger.w(TAG, it)
            return Result.failure(it)
        }

        val request = CreateNewUserInviteRequest(
            email = targetEmail,
            targetType = TARGET_TYPE_VAULT,
            shareRoleId = shareRole.value,
            signature = signature
        )
        return Result.success(request)
    }

    companion object {
        private const val TAG = "InviteToVaultImpl"
        private const val TARGET_TYPE_VAULT = 1
    }
}
