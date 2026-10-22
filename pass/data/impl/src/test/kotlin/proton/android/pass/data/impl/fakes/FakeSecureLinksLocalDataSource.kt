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
import kotlinx.coroutines.flow.flowOf
import proton.android.pass.data.impl.local.securelinks.SecureLinksLocalDataSource
import proton.android.pass.domain.UserId
import proton.android.pass.domain.securelinks.SecureLink
import proton.android.pass.domain.securelinks.SecureLinkId

class FakeSecureLinksLocalDataSource : SecureLinksLocalDataSource {

    private val deletedIds = mutableListOf<SecureLinkId>()

    val deleted: List<SecureLinkId> get() = deletedIds

    override suspend fun create(userId: UserId, secureLink: SecureLink) = Unit

    override suspend fun delete(userId: UserId, secureLinkId: SecureLinkId) {
        deletedIds.add(secureLinkId)
    }

    override suspend fun delete(userId: UserId, secureLinks: List<SecureLink>) = Unit

    override suspend fun deleteAllInactive(userId: UserId) = Unit

    override suspend fun getAll(userId: UserId): List<SecureLink> = emptyList()

    override suspend fun getCount(userId: UserId): Int = 0

    override fun observe(userId: UserId, secureLinkId: SecureLinkId): Flow<SecureLink> = flowOf()

    override fun observeAll(userId: UserId): Flow<List<SecureLink>> = flowOf(emptyList())

    override fun observeCount(userId: UserId): Flow<Int> = flowOf(0)

    override suspend fun read(userId: UserId, secureLinkId: SecureLinkId): SecureLink =
        throw NoSuchElementException("No secure link ${secureLinkId.id}")

    override suspend fun update(userId: UserId, secureLinks: List<SecureLink>) = Unit
}
