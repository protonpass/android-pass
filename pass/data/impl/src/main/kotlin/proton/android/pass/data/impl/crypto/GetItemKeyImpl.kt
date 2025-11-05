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

package proton.android.pass.data.impl.crypto

import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.crypto.GetItemKey
import proton.android.pass.data.api.errors.ItemKeyNotAvailableError
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.impl.local.LocalItemDataSource
import proton.android.pass.data.impl.repositories.ShareKeyRepository
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.key.ItemKey
import javax.inject.Inject

class GetItemKeyImpl @Inject constructor(
    private val shareRepository: ShareRepository,
    private val shareKeyRepository: ShareKeyRepository,
    private val localItemDataSource: LocalItemDataSource
) : GetItemKey {

    override suspend fun invoke(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): ItemKey = when (shareRepository.getById(userId, shareId)) {
        is Share.Item -> shareKeyRepository.getLatestKeyForShare(shareId)
            .first()
            .let { shareKey ->
                ItemKey(
                    rotation = shareKey.rotation,
                    key = shareKey.key,
                    responseKey = shareKey.responseKey
                )
            }

        is Share.Vault -> localItemDataSource.getById(userId, shareId, itemId)
            .let { item ->
                item ?: throw IllegalArgumentException("Item not found")
                item.key ?: throw ItemKeyNotAvailableError()
                item.encryptedKey ?: throw ItemKeyNotAvailableError()
                ItemKey(
                    rotation = item.keyRotation,
                    key = item.encryptedKey,
                    responseKey = item.key
                )
            }
    }
}

