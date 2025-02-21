/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.data.impl.usecases.items

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.items.ObserveSharedItemCountSummary
import proton.android.pass.domain.items.ItemSharedType
import javax.inject.Inject

class ObserveSharedItemCountSummaryImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val shareRepository: ShareRepository,
    private val itemRepository: ItemRepository
) : ObserveSharedItemCountSummary {

    override fun invoke(itemSharedType: ItemSharedType): Flow<ItemCountSummary> = observeCurrentUser()
        .flatMapLatest { user ->
            when (itemSharedType) {
                ItemSharedType.SharedByMe -> itemRepository.observeSharedByMeEncryptedItems(
                    userId = user.userId,
                    itemState = null
                ).mapLatest { encryptedItemsSharedByMe ->
                    encryptedItemsSharedByMe.map { it.shareId }
                }

                ItemSharedType.SharedWithMe -> shareRepository.observeSharedWithMeIds(
                    userId = user.userId
                )
            }.flatMapLatest { sharedShareIds ->
                itemRepository.observeItemCountSummary(
                    userId = user.userId,
                    shareIds = sharedShareIds,
                    itemState = null,
                    onlyShared = true
                )
            }
        }

}
