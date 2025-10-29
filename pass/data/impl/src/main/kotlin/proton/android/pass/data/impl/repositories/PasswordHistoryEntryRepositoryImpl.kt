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

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.PasswordHistoryEntryRepository
import proton.android.pass.data.impl.db.entities.PasswordHistoryEntity
import proton.android.pass.data.impl.local.LocalPasswordHistoryEntryDataSource
import proton.android.pass.domain.PasswordHistoryEntry
import proton.android.pass.domain.PasswordHistoryEntryId
import javax.inject.Inject

class PasswordHistoryEntryRepositoryImpl @Inject constructor(
    private val local: LocalPasswordHistoryEntryDataSource
) : PasswordHistoryEntryRepository {

    override suspend fun addOnePasswordHistoryEntryForUser(userId: UserId, passwordHistoryEntry: PasswordHistoryEntry) {
        local.addOnePasswordHistoryEntryForUser(passwordHistoryEntry.toEntity(userId))
    }

    override suspend fun getPasswordHistoryEntryForUser(userId: UserId): List<PasswordHistoryEntry> =
        local.getPasswordHistoryEntryForUser(userId = userId).map { it.toDomain() }

    override fun observePasswordHistoryEntryForUser(userId: UserId): Flow<List<PasswordHistoryEntry>> {
        return local
            .observePasswordHistoryEntryForUser(userId = userId)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun deletePasswordHistoryEntryForUser(userId: UserId) {
        local.deletePasswordHistoryEntryForUser(userId = userId)
    }

    override suspend fun deleteOnePasswordHistoryEntryForUser(
        userId: UserId,
        passwordHistoryEntryId: PasswordHistoryEntryId
    ) {
        local.deleteOnePasswordHistoryEntryForUser(
            userId = userId,
            passwordHistoryEntryId = passwordHistoryEntryId
        )
    }

    override suspend fun deleteOldPasswordHistoryEntry(beforeTimestamp: Long) {
        local.deleteOldPasswordHistoryEntry(beforeTimestamp)
    }

    private fun PasswordHistoryEntry.toEntity(userId: UserId) = PasswordHistoryEntity(
        password = encrypted,
        createdTime = createdTime,
        userId = userId.id,
        id = 0L // autogenerate
    )

    private fun PasswordHistoryEntity.toDomain() = PasswordHistoryEntry(
        passwordHistoryEntryId = PasswordHistoryEntryId(id = id),
        encrypted = password,
        createdTime = createdTime
    )
}
