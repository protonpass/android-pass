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
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.items.ItemIsBreachedFilter
import proton.android.pass.data.api.usecases.items.ItemSecurityCheckFilter
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareSelection
import javax.inject.Inject

class ObserveItemsImpl @Inject constructor(
    private val itemRepository: ItemRepository,
    private val observeCurrentUser: ObserveCurrentUser
) : ObserveItems {

    override fun invoke(
        selection: ShareSelection,
        itemState: ItemState?,
        filter: ItemTypeFilter,
        userId: UserId?,
        securityCheckFilter: ItemSecurityCheckFilter,
        isBreachedFilter: ItemIsBreachedFilter
    ): Flow<List<Item>> = if (userId == null) {
        observeCurrentUser()
            .flatMapLatest {
                observeItems(
                    userId = it.userId,
                    selection = selection,
                    itemState = itemState,
                    filter = filter,
                    exclusionFilter = securityCheckFilter,
                    isBreachedFilter = isBreachedFilter
                )
            }
    } else {
        observeItems(userId, selection, itemState, filter, securityCheckFilter, isBreachedFilter)
    }

    @Suppress("LongParameterList")
    private fun observeItems(
        userId: UserId,
        selection: ShareSelection,
        itemState: ItemState?,
        filter: ItemTypeFilter,
        exclusionFilter: ItemSecurityCheckFilter,
        isBreachedFilter: ItemIsBreachedFilter
    ): Flow<List<Item>> = itemRepository.observeItems(
        userId = userId,
        shareSelection = selection,
        itemState = itemState,
        itemTypeFilter = filter
    ).map { items ->
        items.filter { item ->
            when (exclusionFilter) {
                ItemSecurityCheckFilter.Excluded -> item.hasSkippedHealthCheck
                ItemSecurityCheckFilter.Included -> !item.hasSkippedHealthCheck
                ItemSecurityCheckFilter.All -> true
            } && when (isBreachedFilter) {
                ItemIsBreachedFilter.Breached -> item.isEmailBreached
                ItemIsBreachedFilter.NotBreached -> !item.isEmailBreached
                ItemIsBreachedFilter.All -> true
            }
        }
    }
}

