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

package proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.SearchEntry
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

interface SearchEntryRepository {
    suspend fun store(userId: UserId, shareId: ShareId, itemId: ItemId)
    suspend fun deleteAll(userId: UserId)
    suspend fun deleteAllByShare(shareId: ShareId)
    suspend fun deleteEntry(shareId: ShareId, itemId: ItemId)
    fun observeAll(userId: UserId): Flow<List<SearchEntry>>
    fun observeAllByShare(shareId: ShareId): Flow<List<SearchEntry>>
}
