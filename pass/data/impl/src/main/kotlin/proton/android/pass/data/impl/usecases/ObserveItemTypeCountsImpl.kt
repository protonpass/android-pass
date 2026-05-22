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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ItemTypeCounts
import proton.android.pass.data.api.repositories.SearchIndexRepository
import proton.android.pass.data.api.usecases.ObserveItemTypeCounts
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.items.ItemSharedType
import javax.inject.Inject

class ObserveItemTypeCountsImpl @Inject constructor(
    private val searchIndexRepository: SearchIndexRepository
) : ObserveItemTypeCounts {

    override fun invoke(
        userId: UserId,
        shareIds: List<ShareId>?,
        folderId: FolderId?,
        itemState: ItemState?,
        itemSharedType: ItemSharedType?,
        query: String?
    ): Flow<ItemTypeCounts> = searchIndexRepository.observeItemTypeCounts(
        userId = userId,
        shareIds = shareIds,
        folderId = folderId,
        itemState = itemState,
        itemSharedType = itemSharedType,
        query = query
    )
}
