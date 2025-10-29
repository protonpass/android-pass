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

package proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.PasswordHistoryEntity
import proton.android.pass.domain.PasswordHistoryEntryId
import javax.inject.Inject

class LocalPasswordHistoryEntryDataSourceImpl @Inject constructor(
    private val database: PassDatabase
) : LocalPasswordHistoryEntryDataSource {

    override suspend fun addOnePasswordHistoryEntryForUser(passwordHistoryEntity: PasswordHistoryEntity) {
        database
            .passwordHistoryDao()
            .insertOrUpdate(passwordHistoryEntity)
    }

    override suspend fun getPasswordHistoryEntryForUser(userId: UserId): List<PasswordHistoryEntity> {
        return database
            .passwordHistoryDao()
            .getPasswordHistoryForUser(userId = userId.id)
    }

    override fun observePasswordHistoryEntryForUser(userId: UserId): Flow<List<PasswordHistoryEntity>> {
        return database
            .passwordHistoryDao()
            .observePasswordHistoryForUser(userId = userId.id)
    }

    override suspend fun deletePasswordHistoryEntryForUser(userId: UserId) {
        database.passwordHistoryDao().deletePasswordHistoryForUser(userId = userId.id)
    }

    override suspend fun deleteOnePasswordHistoryEntryForUser(
        userId: UserId,
        passwordHistoryEntryId: PasswordHistoryEntryId
    ) {
        database.passwordHistoryDao().deleteOnePasswordHistoryForUser(
            userId = userId.id,
            id = passwordHistoryEntryId.id
        )
    }

    override suspend fun deleteOldPasswordHistoryEntry(beforeTimestamp: Long) {
        database.passwordHistoryDao().deleteHistoryOlderThan(beforeTimestamp = beforeTimestamp)
    }
}

