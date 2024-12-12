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
import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.data.api.crypto.GetItemKeys
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.impl.repositories.ItemKeyRepository
import proton.android.pass.data.impl.repositories.ShareKeyRepository
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.key.ItemKey
import proton.android.pass.domain.key.ShareKey
import javax.inject.Inject

class GetItemKeysImpl @Inject constructor(
    private val shareRepository: ShareRepository,
    private val shareKeyRepository: ShareKeyRepository,
    private val itemKeyRepository: ItemKeyRepository
) : GetItemKeys {

    override suspend fun invoke(
        userAddress: UserAddress,
        shareId: ShareId,
        itemId: ItemId
    ): Pair<ShareKey, ItemKey> = shareRepository.getById(userAddress.userId, shareId).let { share ->
        when (share) {
            is Share.Item -> shareKeyRepository.getLatestKeyForShare(shareId)
                .first()
                .let { shareKey ->
                    shareKey to ItemKey(
                        rotation = shareKey.rotation,
                        key = shareKey.key,
                        responseKey = shareKey.responseKey
                    )
                }

            is Share.Vault -> itemKeyRepository.getLatestItemKey(
                userId = userAddress.userId,
                addressId = userAddress.addressId,
                shareId = shareId,
                itemId = itemId
            ).first()
        }
    }

}
