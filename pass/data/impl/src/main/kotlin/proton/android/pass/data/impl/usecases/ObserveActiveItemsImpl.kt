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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareSelection
import javax.inject.Inject

class ObserveActiveItemsImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val observeAllShares: ObserveAllShares,
    private val itemRepository: ItemRepository
) : ObserveActiveItems {

    override operator fun invoke(
        filter: ItemTypeFilter,
        shareSelection: ShareSelection
    ): Flow<List<Item>> = observeCurrentUser()
        .filterNotNull()
        .combine(observeAllShares()) { user, shares ->
            user to shares.map { share -> share.id }
        }
        .distinctUntilChanged()
        .flatMapLatest { pair ->
            itemRepository.observeItems(
                userId = pair.first.userId,
                shareSelection = shareSelection,
                itemState = ItemState.Active,
                itemTypeFilter = filter
            )
        }
        .distinctUntilChanged()
}
