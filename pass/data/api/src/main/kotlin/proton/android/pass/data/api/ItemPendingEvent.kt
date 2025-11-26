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

package proton.android.pass.data.api

import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.data.api.repositories.UpdateShareEvent
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

data class ItemPendingEvent(
    val userId: UserId,
    val shareId: ShareId,
    val addressId: AddressId,
    val lastEventId: String,
    val updateShareEvent: UpdateShareEvent?,
    private val pendingEventLists: Set<PendingEventList>
) {

    val pendingItemRevisions: List<PendingEventItemRevision> by lazy {
        pendingEventLists.flatMap { pendingEvent ->
            pendingEvent.updatedItems
        }
    }

    val hasPendingItemRevisions: Boolean = pendingItemRevisions.isNotEmpty()

    val deletedItemIds: List<ItemId> by lazy {
        pendingEventLists.flatMap { pendingEvent ->
            pendingEvent.deletedItemIds.map { deletedItemId ->
                ItemId(deletedItemId)
            }
        }
    }

    val hasDeletedItemIds: Boolean = deletedItemIds.isNotEmpty()

    val hasPendingChanges: Boolean = hasPendingItemRevisions ||
        hasDeletedItemIds ||
        updateShareEvent != null

    val hasPendingChangesAndIsGroup: Boolean =
        hasPendingChanges && updateShareEvent?.groupId != null

}
