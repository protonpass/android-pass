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

package proton.android.pass.data.impl.usecases.items

import kotlinx.coroutines.flow.Flow
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.items.ObserveMonitoredItems
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareSelection
import javax.inject.Inject

class ObserveMonitoredItemsImpl @Inject constructor(
    private val observeItems: ObserveItems
) : ObserveMonitoredItems {

    override fun invoke(): Flow<List<Item>> = observeItems(
        selection = ShareSelection.AllShares,
        itemState = ItemState.Active,
        filter = ItemTypeFilter.Logins,
        itemFlags = mapOf(ItemFlag.SkipHealthCheck to false)
    )

}
