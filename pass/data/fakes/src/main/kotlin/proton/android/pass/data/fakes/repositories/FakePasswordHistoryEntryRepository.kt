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

package proton.android.pass.data.fakes.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.PasswordHistoryEntryRepository
import proton.android.pass.domain.PasswordHistoryEntry
import proton.android.pass.domain.PasswordHistoryEntryId
import javax.inject.Inject

@Suppress("NotImplementedDeclaration")
class FakePasswordHistoryEntryRepository @Inject constructor() : PasswordHistoryEntryRepository {
    override suspend fun addOnePasswordHistoryEntryForUser(userId: UserId, passwordHistoryEntry: PasswordHistoryEntry) {
    }

    override suspend fun getPasswordHistoryEntryForUser(userId: UserId): List<PasswordHistoryEntry> = emptyList()

    override fun observePasswordHistoryEntryForUser(userId: UserId): Flow<List<PasswordHistoryEntry>> = emptyFlow()

    override suspend fun deletePasswordHistoryEntryForUser(userId: UserId) {}
    override suspend fun deleteOnePasswordHistoryEntryForUser(
        userId: UserId,
        passwordHistoryEntryId: PasswordHistoryEntryId
    ) {
    }

    override suspend fun deleteOldPasswordHistoryEntry(beforeTimestamp: Long) {}
}
