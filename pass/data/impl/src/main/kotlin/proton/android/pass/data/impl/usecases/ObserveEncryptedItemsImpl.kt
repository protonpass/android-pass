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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveEncryptedItems
import proton.android.pass.domain.ItemEncrypted
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareSelection
import javax.inject.Inject

class ObserveEncryptedItemsImpl @Inject constructor(
    private val syncStatusRepository: ItemSyncStatusRepository,
    private val itemRepository: ItemRepository,
    private val observeCurrentUser: ObserveCurrentUser
) : ObserveEncryptedItems {

    override fun invoke(
        selection: ShareSelection,
        itemState: ItemState?,
        filter: ItemTypeFilter,
        userId: UserId?,
        itemFlags: Map<ItemFlag, Boolean>
    ): Flow<List<ItemEncrypted>> = syncStatusRepository.observeSyncState()
        .filter { syncState -> !syncState.isVisibleSyncing }
        .flatMapLatest {
            if (userId == null) {
                observeCurrentUser()
                    .flatMapLatest { currentUser ->
                        observeItems(
                            userId = currentUser.userId,
                            selection = selection,
                            itemState = itemState,
                            filter = filter,
                            itemFlags = itemFlags
                        )
                    }
            } else {
                observeItems(
                    userId = userId,
                    selection = selection,
                    itemState = itemState,
                    filter = filter,
                    itemFlags = itemFlags
                )
            }
        }

    @Suppress("LongParameterList")
    private fun observeItems(
        userId: UserId,
        selection: ShareSelection,
        itemState: ItemState?,
        filter: ItemTypeFilter,
        itemFlags: Map<ItemFlag, Boolean>
    ): Flow<List<ItemEncrypted>> {
        val (setFlags, clearFlags) = itemFlags.entries.partition { it.value }
        return itemRepository.observeEncryptedItems(
            userId = userId,
            shareSelection = selection,
            itemState = itemState,
            itemTypeFilter = filter,
            setFlags = foldFlags(setFlags.map { it.key }),
            clearFlags = foldFlags(clearFlags.map { it.key })
        )
    }

    private fun foldFlags(flags: List<ItemFlag>): Int = flags.fold(0) { acc, flag -> acc or flag.value }
}
