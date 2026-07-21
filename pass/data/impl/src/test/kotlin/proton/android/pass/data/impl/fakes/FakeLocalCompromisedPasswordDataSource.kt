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

package proton.android.pass.data.impl.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.entities.CompromisedPasswordEntity
import proton.android.pass.data.impl.local.LocalCompromisedPasswordDataSource
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

class FakeLocalCompromisedPasswordDataSource : LocalCompromisedPasswordDataSource {

    private val state = MutableStateFlow<Map<Triple<String, String, String>, CompromisedPasswordEntity>>(emptyMap())

    fun seed(entities: List<CompromisedPasswordEntity>) {
        state.value = entities.associateBy { Triple(it.userId, it.shareId, it.itemId) }
    }

    fun snapshot(): List<CompromisedPasswordEntity> = state.value.values.toList()

    override fun observeCompromisedItems(userId: UserId): Flow<List<CompromisedPasswordEntity>> =
        state.map { map -> map.values.filter { it.userId == userId.id && it.isCompromised } }

    override fun observeIsCompromised(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<Boolean> = state.map { map ->
        map[Triple(userId.id, shareId.id, itemId.id)]?.isCompromised == true
    }

    override suspend fun getAllCheckedItems(userId: UserId): List<CompromisedPasswordEntity> =
        state.value.values.filter { it.userId == userId.id }

    override suspend fun upsertAll(entities: List<CompromisedPasswordEntity>) {
        state.value = state.value.toMutableMap().apply {
            entities.forEach { put(Triple(it.userId, it.shareId, it.itemId), it) }
        }
    }

    override suspend fun deleteAllForUser(userId: UserId) {
        state.value = state.value.filterValues { it.userId != userId.id }
    }
}
