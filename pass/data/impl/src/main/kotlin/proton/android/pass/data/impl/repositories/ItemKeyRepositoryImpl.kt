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

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.crypto.api.error.KeyNotFound
import proton.android.pass.crypto.api.usecases.OpenItemKey
import proton.android.pass.data.impl.extensions.toCrypto
import proton.android.pass.data.impl.remote.RemoteItemKeyDataSource
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.key.ItemKey
import proton.android.pass.domain.key.ShareKey
import javax.inject.Inject

class ItemKeyRepositoryImpl @Inject constructor(
    private val shareKeyRepository: ShareKeyRepository,
    private val remoteItemKeyRepository: RemoteItemKeyDataSource,
    private val openItemKey: OpenItemKey
) : ItemKeyRepository {

    override fun getLatestItemKey(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        itemId: ItemId,
        scope: ItemKeyRepository.Scope.SharedVault
    ): Flow<ItemKey> = flow {
        val response = remoteItemKeyRepository.fetchLatestItemKey(userId, shareId, itemId)
        val inviteKey = when (val source = scope.decryptionSource) {
            ItemKeyRepository.VaultDecryptionSource.Share -> resolveShareKey(
                userId = userId,
                addressId = addressId,
                shareId = shareId,
                groupEmail = scope.groupEmail,
                keyRotation = response.keyRotation
            )

            is ItemKeyRepository.VaultDecryptionSource.Folder -> source.folderKey
        }
        emit(openItemKey(inviteKey, response.toCrypto()))
    }

    override fun getLatestShareAndItemKey(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        itemId: ItemId,
        scope: ItemKeyRepository.Scope
    ): Flow<Pair<ShareKey, ItemKey>> = flow {
        when (scope) {
            ItemKeyRepository.Scope.SharedItem -> {
                val shareKey = shareKeyRepository.getLatestKeyForShare(shareId).first()
                emit(
                    shareKey to ItemKey(
                        rotation = shareKey.rotation,
                        key = shareKey.key,
                        responseKey = shareKey.responseKey
                    )
                )
            }

            is ItemKeyRepository.Scope.SharedVault -> {
                val response = remoteItemKeyRepository.fetchLatestItemKey(userId, shareId, itemId)
                val shareKey = resolveShareKey(
                    userId = userId,
                    addressId = addressId,
                    shareId = shareId,
                    groupEmail = scope.groupEmail,
                    keyRotation = response.keyRotation
                )
                val itemKey = openItemKey(
                    when (val source = scope.decryptionSource) {
                        ItemKeyRepository.VaultDecryptionSource.Share -> shareKey
                        is ItemKeyRepository.VaultDecryptionSource.Folder -> source.folderKey
                    },
                    response.toCrypto()
                )
                emit(shareKey to itemKey)
            }
        }
    }

    private suspend fun resolveShareKey(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        groupEmail: String?,
        keyRotation: Long
    ): ShareKey = shareKeyRepository
        .getShareKeyForRotation(
            userId = userId,
            addressId = addressId,
            shareId = shareId,
            groupEmail = groupEmail,
            keyRotation = keyRotation
        )
        .first()
        ?: throw KeyNotFound(
            "Could not find ShareKey [shareId=${shareId.id}] [keyRotation=$keyRotation]"
        )
}
