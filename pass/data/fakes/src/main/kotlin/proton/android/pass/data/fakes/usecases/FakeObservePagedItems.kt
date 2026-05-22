/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.fakes.usecases

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.SearchSortBy
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObservePagedItems
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.items.ItemSharedType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeObservePagedItems @Inject constructor() : ObservePagedItems {

    private val flow = MutableSharedFlow<PagingData<Item>>(replay = 1)

    var lastShareIds: List<ShareId>? = null
        private set
    var lastFolderId: FolderId? = null
        private set

    fun emitValue(value: PagingData<Item>) {
        flow.tryEmit(value)
    }

    fun emitEmpty() {
        flow.tryEmit(PagingData.empty())
    }

    override fun invoke(
        userId: UserId,
        query: String?,
        sortBy: SearchSortBy,
        shareIds: List<ShareId>?,
        folderId: FolderId?,
        itemState: ItemState?,
        itemSharedType: ItemSharedType?,
        itemTypeFilter: ItemTypeFilter
    ): Flow<PagingData<Item>> {
        lastShareIds = shareIds
        lastFolderId = folderId
        return flow
    }
}
