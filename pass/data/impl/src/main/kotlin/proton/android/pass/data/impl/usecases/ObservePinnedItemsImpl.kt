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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObservePinnedItems
import proton.android.pass.domain.Item
import proton.android.pass.domain.ShareSelection
import javax.inject.Inject

class ObservePinnedItemsImpl @Inject constructor(
    private val itemRepository: ItemRepository,
    private val observeCurrentUser: ObserveCurrentUser
) : ObservePinnedItems {

    override fun invoke(
        userId: UserId?,
        filter: ItemTypeFilter,
        shareSelection: ShareSelection,
        includeHidden: Boolean
    ): Flow<List<Item>> = if (userId != null) {
        itemRepository.observePinnedItems(
            userId = userId,
            shareSelection = shareSelection,
            itemTypeFilter = filter,
            includeHidden = includeHidden
        )
    } else {
        observeCurrentUser().flatMapLatest {
            itemRepository.observePinnedItems(
                userId = it.userId,
                shareSelection = shareSelection,
                itemTypeFilter = filter,
                includeHidden = includeHidden
            )
        }
    }
}

