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

package proton.android.pass.data.impl.usecases.shares

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.shares.ObserveShareItemsCount
import proton.android.pass.domain.ShareId
import javax.inject.Inject

class ObserveShareItemsCountImpl @Inject constructor(
    private val itemRepository: ItemRepository
) : ObserveShareItemsCount {

    override fun invoke(shareId: ShareId): Flow<Int> = itemRepository.observeItemCount(listOf(shareId))
        .mapLatest { shareItemCountMap ->
            shareItemCountMap[shareId]
                ?.totalItems
                ?.toInt()
                ?: 0
        }

}
