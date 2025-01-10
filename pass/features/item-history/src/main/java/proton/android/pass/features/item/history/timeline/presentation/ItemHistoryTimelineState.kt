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

package proton.android.pass.features.item.history.timeline.presentation

import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.items.ItemCategory

internal sealed class ItemHistoryTimelineState(internal val itemCategory: ItemCategory) {

    data object Error : ItemHistoryTimelineState(ItemCategory.Unknown)

    data object Loading : ItemHistoryTimelineState(ItemCategory.Unknown)

    data class Success(
        internal val shareId: ShareId,
        internal val itemId: ItemId,
        internal val itemRevisions: ImmutableList<ItemRevision>,
        private val itemRevisionCategory: ItemCategory
    ) : ItemHistoryTimelineState(itemRevisionCategory)

    internal val showOptions: Boolean
        get() = this is Success && itemRevisions.size > 1
}
