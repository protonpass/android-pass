/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.Option
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

sealed interface ParentContainer {
    data object Share : ParentContainer
    data class Folder(val folderId: FolderId) : ParentContainer
}

typealias BulkMoveToVaultSelection = Map<ShareId, Map<ParentContainer, Set<ItemId>>>

fun Map<ShareId, List<ItemId>>.toBulkMoveToVaultSelection(): BulkMoveToVaultSelection = mapValues { (_, itemIds) ->
    mapOf(ParentContainer.Share to itemIds.toSet())
}

fun BulkMoveToVaultSelection.flattenByShare(): Map<ShareId, List<ItemId>> = mapValues { (_, containers) ->
    containers.values.flatten().distinct()
}

sealed interface BulkMoveToVaultEvent {
    data object Idle : BulkMoveToVaultEvent
    data object Completed : BulkMoveToVaultEvent
}

interface BulkMoveToVaultRepository {
    suspend fun save(value: BulkMoveToVaultSelection)
    fun observe(): Flow<Option<BulkMoveToVaultSelection>>
    suspend fun delete()

    suspend fun emitEvent(event: BulkMoveToVaultEvent)
    fun observeEvent(): Flow<BulkMoveToVaultEvent>
}
