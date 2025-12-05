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
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.test.domain.items.ItemIdTestFactory
import proton.android.pass.test.domain.shares.ShareIdTestFactory

internal object ItemHistoryTimelineStateTestFactory {

    internal object Error {

        internal fun create(): ItemHistoryTimelineState.Error = ItemHistoryTimelineState.Error

    }

    internal object Loading {

        internal fun create(): ItemHistoryTimelineState.Loading = ItemHistoryTimelineState.Loading

    }

    internal object Success {

        internal fun create(
            shareId: ShareId = ShareIdTestFactory.create(),
            itemId: ItemId = ItemIdTestFactory.create(),
            itemRevisions: ImmutableList<ItemRevision> = persistentListOf(),
            itemRevisionCategory: ItemCategory = ItemCategory.Unknown
        ): ItemHistoryTimelineState.Success = ItemHistoryTimelineState.Success(
            shareId = shareId,
            itemId = itemId,
            itemRevisions = itemRevisions,
            itemRevisionCategory = itemRevisionCategory
        )
    }

}
