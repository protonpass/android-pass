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
import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.extension.primary
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import proton.android.pass.common.api.flatMap
import proton.android.pass.crypto.api.usecases.EncryptInviteKeys
import proton.android.pass.data.api.usecases.InviteToVault
import proton.android.pass.data.impl.remote.RemoteInviteDataSource
import proton.android.pass.data.impl.repositories.ShareKeyRepository
import proton.android.pass.data.impl.requests.CreateInviteKey
import proton.android.pass.data.impl.requests.CreateInviteRequest
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.ShareId
import javax.inject.Inject

class InviteToVaultImpl @Inject constructor(
    private val publicAddressRepository: PublicAddressRepository,
    private val userAddressRepository: UserAddressRepository,
    private val accountManager: AccountManager,
    private val encryptInviteKeys: EncryptInviteKeys,
    private val shareKeyRepository: ShareKeyRepository,
    private val remoteInviteDataSource: RemoteInviteDataSource
) : InviteToVault {

    override suspend fun invoke(
        userId: UserId?,
        targetEmail: String,
        shareId: ShareId
    ): Result<Unit> {
        val id = userId ?: run {
            println("BEFORE: ${System.currentTimeMillis()}")
            val primaryUserId = accountManager.getPrimaryUserId().firstOrNull()
            println("AFTER: ${System.currentTimeMillis()}")
            if (primaryUserId == null) {
                PassLogger.w(TAG, "No primary user")
                return Result.failure(IllegalStateException("No primary user"))
            }
            primaryUserId
        }

        val address = runCatching { userAddressRepository.getAddresses(id) }
            .fold(
                onSuccess = {
                    it.primary()
                        ?: return Result.failure(IllegalStateException("No primary address for inviter user"))
                },
                onFailure = {
                    PassLogger.w(TAG, it, "Failed to get user addresses")
                    return Result.failure(it)
                }
            )

        return buildRequest(
            userId = id,
            shareId = shareId,
            address = address,
            targetEmail = targetEmail
        ).flatMap { request ->
            remoteInviteDataSource.sendInvite(id, shareId, request)
        }
    }

    @Suppress("ReturnCount")
    private suspend fun buildRequest(
        userId: UserId,
        shareId: ShareId,
        address: UserAddress,
        targetEmail: String
    ): Result<CreateInviteRequest> {
        val shareKeys = shareKeyRepository.getShareKeys(
            userId = userId,
            addressId = address.addressId,
            shareId = shareId,
            forceRefresh = true
        ).first()

        val inviterAddressKey = address.keys.primary()?.privateKey
            ?: return Result.failure(IllegalStateException("No primary address key for invited user"))

        val targetUserAddress = runCatching {
            publicAddressRepository.getPublicAddress(userId, targetEmail)
        }.fold(
            onSuccess = { it },
            onFailure = {
                PassLogger.w(TAG, it, "Failed to get public addresses")
                return Result.failure(it)
            }
        )

        val encryptedKeys = runCatching {
            encryptInviteKeys(
                inviterAddressKey = inviterAddressKey,
                shareKeys = shareKeys,
                targetAddressKey = targetUserAddress.primaryKey.publicKey
            )
        }.fold(
            onSuccess = { it },
            onFailure = {
                PassLogger.w(TAG, it, "Failed to encrypt invite keys")
                return Result.failure(it)
            }
        )

        val request = CreateInviteRequest(
            keys = encryptedKeys.keys.map {
                CreateInviteKey(
                    key = it.key,
                    keyRotation = it.keyRotation
                )
            },
            email = targetEmail,
            targetType = TARGET_TYPE_VAULT,
        )

        return Result.success(request)
    }

    companion object {
        private const val TAG = "InviteToVaultImpl"
        private const val TARGET_TYPE_VAULT = 1
    }
}
